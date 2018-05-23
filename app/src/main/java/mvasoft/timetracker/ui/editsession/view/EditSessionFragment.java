package mvasoft.timetracker.ui.editsession.view;

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
import android.widget.EditText;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import mvasoft.datetimepicker.DatePickerFragment;
import mvasoft.datetimepicker.TimePickerFragment;
import mvasoft.datetimepicker.event.DatePickerDateSelectedEvent;
import mvasoft.datetimepicker.event.TimePickerTimeSelectedEvent;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentEditSessionBinding;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.editsession.viewmodel.EditSessionViewModel;

public class EditSessionFragment extends BindingSupportFragment<FragmentEditSessionBinding,
        EditSessionViewModel> {

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

        makeEditNotEditable(getBinding().edtStartDate);
        makeEditNotEditable(getBinding().edtStartTime);
        makeEditNotEditable(getBinding().edtEndDate);
        makeEditNotEditable(getBinding().edtEndTime);

        return v;
    }

    private void makeEditNotEditable(EditText edit) {
        if (edit == null)
            return;

        edit.setKeyListener(null);
        edit.setFocusable(false);
        edit.setFocusableInTouchMode(false);
        edit.setClickable(true);
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


    long getSessionId() {
        return getViewModel().getModel().getSessionId();
    }

    private String makeTimePickerTag(boolean forStart) {
        return "EditSessionFragment: id = " + getViewModel().getModel().getSessionId() +
                "; forStart = " + forStart;
    }

    @Override
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe
    public void onDatePickerDateSelectedEvent(DatePickerDateSelectedEvent e) {
        boolean isStart = e.tag.equals(makeTimePickerTag(true));
        if (!isStart && !e.tag.equals(makeTimePickerTag(false)))
            return;

        if (isStart) {
            getViewModel().getModel().setStartDate(e.year, e.month, e.dayOfMonth);
            if (mPreferences.syncStartEndDate())
                getViewModel().getModel().setEndDate(e.year, e.month, e.dayOfMonth);
        }
        else {
            getViewModel().getModel().setEndDate(e.year, e.month, e.dayOfMonth);
            if (mPreferences.syncStartEndDate())
                getViewModel().getModel().setStartDate(e.year, e.month, e.dayOfMonth);
        }
    }

    @Subscribe
    public void onTimePickerTimeSelectedEvent(TimePickerTimeSelectedEvent e) {
        boolean isStart = e.tag.equals(makeTimePickerTag(true));
        if (!isStart && !e.tag.equals(makeTimePickerTag(false)))
            return;

        if (isStart) {
            getViewModel().getModel().setStartTime(e.hourOfDay, e.minute);
        }
        else {
            getViewModel().getModel().setEndTime(e.hourOfDay, e.minute);
        }
    }


    private void editDate(long unixTime, boolean isStart) {
        if (getFragmentManager() == null)
            return;

        String eventTag = makeTimePickerTag(isStart);
        if (getFragmentManager().findFragmentByTag(eventTag + "date") != null)
            return;

        DatePickerFragment f = DatePickerFragment.newInstante(eventTag, unixTime);
        f.show(getFragmentManager(), eventTag + "date");
    }


    private void editTime(long unixTime, boolean isStart) {
        if (getFragmentManager() == null)
            return;

        String eventTag = makeTimePickerTag(isStart);
        if (getFragmentManager().findFragmentByTag(eventTag + "time") != null)
            return;

        TimePickerFragment f = TimePickerFragment.newInstante(eventTag, unixTime);
        f.show(getFragmentManager(), eventTag + "time");
    }

}
