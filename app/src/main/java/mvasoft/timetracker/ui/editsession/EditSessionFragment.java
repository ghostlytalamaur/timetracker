package mvasoft.timetracker.ui.editsession;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import javax.inject.Inject;

import mvasoft.dialogs.DatePickerFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.dialogs.TimePickerFragment;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentEditSessionBinding;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.common.EditTextUtils;

public class EditSessionFragment extends BindingSupportFragment<FragmentEditSessionBinding,
        EditSessionViewModel> implements DialogResultListener {

    private static final int DLG_REQUEST_START_DATE = 1;
    private static final int DLG_REQUEST_END_DATE = 2;
    private static final int DLG_REQUEST_START_TIME = 3;
    private static final int DLG_REQUEST_END_TIME = 4;

    private static final String ARGS_SESSION_ID = "session_id";

    @Inject
    ViewModelProvider.Factory mFactory;

    @Inject
    AppPreferences mPreferences;

    static public EditSessionFragment newInstance(long sessionId) {

        Bundle args = new Bundle();
        args.putLong(ARGS_SESSION_ID, sessionId);

        EditSessionFragment fragment = new EditSessionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            getViewModel().getModel().restoreState(savedInstanceState);
        else if (getArguments() != null) {
            long id = getArguments().getLong(ARGS_SESSION_ID, -1);
            if (id >= 0)
                getViewModel().getModel().setId(id);
        }

        setHasOptionsMenu(true);
    }

    private long getUnixTimeValue(LiveData<Long> data) {
        if (data == null || data.getValue() == null)
            return System.currentTimeMillis() / 1000;
        else
            return data.getValue();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        getBinding().edtStartTime.setOnClickListener(view ->
                editTime(getUnixTimeValue(getViewModel().getModel().getStartData()), true));

        getBinding().edtEndTime.setOnClickListener(view ->
                editTime(getUnixTimeValue(getViewModel().getModel().getEndData()), false));

        getBinding().edtStartDate.setOnClickListener(view ->
                editDate(getUnixTimeValue(getViewModel().getModel().getStartData()), true));

        getBinding().edtEndDate.setOnClickListener(view ->
                editDate(getUnixTimeValue(getViewModel().getModel().getEndData()), false));

        getBinding().swIsRunning.setOnCheckedChangeListener((buttonView, isChecked) ->
                getViewModel().setIsRunning(isChecked));

        EditTextUtils.makeEditNotEditable(getBinding().edtStartDate);
        EditTextUtils.makeEditNotEditable(getBinding().edtStartTime);
        EditTextUtils.makeEditNotEditable(getBinding().edtEndDate);
        EditTextUtils.makeEditNotEditable(getBinding().edtEndTime);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_session, menu);
        MenuItem item = menu.findItem(R.id.menu_save);
        item.setEnabled(getViewModel().getIsChanged().getValue() != null && getViewModel().getIsChanged().getValue());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                getViewModel().saveSession();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected EditSessionViewModel onCreateViewModel() {
        EditSessionViewModel vm = ViewModelProviders.of(this, mFactory).get(EditSessionViewModel.class);
        vm.getIsChanged().observe(this, aBoolean -> {
            if (getActivity() != null) getActivity().invalidateOptionsMenu();
        });
        return vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_edit_session;
    }

    @Override
    protected int getModelVariableId() {
        return BR.view_model;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getViewModel().getModel().saveState(outState);
    }


    private String makeDialogFragmentTag(boolean forStart) {
        return "EditSessionFragment: id = " + getViewModel().getModel().getSessionId() +
                "; forStart = " + forStart;
    }

    public void onDatePickerDateSelectedEvent(@NonNull DatePickerFragment.DatePickerDialogResultData data) {
        CalendarDay day = data.getDay();
        if (data.requestCode == DLG_REQUEST_START_DATE) {
            getViewModel().getModel().setStartDate(day.getYear(), day.getMonth(), day.getDay());
            if (mPreferences.syncStartEndDate())
                getViewModel().getModel().setEndDate(day.getYear(), day.getMonth(), day.getDay());
        } else {
            getViewModel().getModel().setEndDate(day.getYear(), day.getMonth(), day.getDay());
            if (mPreferences.syncStartEndDate())
                getViewModel().getModel().setStartDate(day.getYear(), day.getMonth(), day.getDay());
        }
    }

    public void onTimePickerTimeSelectedEvent(@NonNull TimePickerFragment.TimePickerDialogResultData data) {
        if (data.requestCode == DLG_REQUEST_START_TIME) {
            getViewModel().getModel().setStartTime(data.hourOfDay, data.minute);
        } else {
            getViewModel().getModel().setEndTime(data.hourOfDay, data.minute);
        }
    }

    private void editDate(long unixTime, boolean isStart) {
        String tag = makeDialogFragmentTag(isStart) + "date";
        int requestCode = isStart ? DLG_REQUEST_START_DATE : DLG_REQUEST_END_DATE;
        new DatePickerFragment.Builder(requestCode)
                .withUnixTime(unixTime)
                .show(this, tag);
    }

    private void editTime(long unixTime, boolean isStart) {
        String tag = makeDialogFragmentTag(isStart) + "time";
        int requestCode = isStart ? DLG_REQUEST_START_TIME : DLG_REQUEST_END_TIME;
        new TimePickerFragment.Builder(requestCode)
                .withUnixTime(unixTime)
                .show(this, tag);
    }

    @Override
    public void onDialogResult(@NonNull DialogResultData data) {
        switch (data.requestCode) {
            case DLG_REQUEST_START_DATE:
            case DLG_REQUEST_END_DATE:
                onDatePickerDateSelectedEvent((DatePickerFragment.DatePickerDialogResultData) data);
                break;

            case DLG_REQUEST_START_TIME:
            case DLG_REQUEST_END_TIME:
                onTimePickerTimeSelectedEvent((TimePickerFragment.TimePickerDialogResultData) data);
                break;
        }
    }
}
