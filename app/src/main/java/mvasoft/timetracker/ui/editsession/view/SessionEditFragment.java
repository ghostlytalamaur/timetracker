package mvasoft.timetracker.ui.editsession.view;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import mvasoft.datetimepicker.DateTimePickerFragment;
import mvasoft.datetimepicker.TimePickerFragment;
import mvasoft.datetimepicker.event.DatePickerDateSelectedEvent;
import mvasoft.datetimepicker.event.TimePickerTimeSelectedEvent;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentSessionEditBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.editsession.viewmodel.EditSessionFragmentViewModel;

public class SessionEditFragment extends BindingSupportFragment<FragmentSessionEditBinding,
        EditSessionFragmentViewModel> {

    private static final String ARGS_SESSION_ID = "session_id";
    private static final int REQUEST_START_TIME = 1;
    private static final int REQUEST_END_TIME = 2;

    @Inject
    ViewModelProvider.Factory mFactory;

    static public SessionEditFragment newInstance(long sessionId) {

        Bundle args = new Bundle();
        args.putLong(ARGS_SESSION_ID, sessionId);

        SessionEditFragment fragment = new SessionEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            long newDateTime;
            switch (requestCode) {
                case REQUEST_START_TIME:
                    newDateTime = data.getLongExtra(DateTimePickerFragment.ARGS_DATE, -1);
                    if (newDateTime > 0)
                        getViewModel().getModel().setStartTime(newDateTime);
                    break;
                case REQUEST_END_TIME:
                    newDateTime = data.getLongExtra(DateTimePickerFragment.ARGS_DATE, -1);
                    if (newDateTime > 0)
                        getViewModel().getModel().setEndTime(newDateTime);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            getViewModel().getModel().restoreState(savedInstanceState);

        if (getArguments() != null) {
            long id = getArguments().getLong(ARGS_SESSION_ID, -1);
            if (id >= 0)
                getViewModel().getModel().setId(id);
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // TODO: 04.05.2018 refactor this. Use actionHandler as in other places
        getBinding().edtStartTime.setOnClickListener(view -> {
            Long dt = getViewModel().getModel().getStartData().getValue();
            if (dt == null)
                dt = System.currentTimeMillis() / 1000;

            editTime(dt, true);
        });

        getBinding().edtEndTime.setOnClickListener(view -> {
            Long dt = getViewModel().getModel().getEndData().getValue();
            if (dt == null)
                dt = System.currentTimeMillis() / 1000;

            editTime(dt, false);
        });

        getBinding().edtStartDate.setOnClickListener(view -> {
            Long dt = getViewModel().getModel().getStartData().getValue();
            if (dt == null)
                dt = System.currentTimeMillis() / 1000;
            editDate(dt, true);
        });

        getBinding().edtEndDate.setOnClickListener(view -> {
            Long dt = getViewModel().getModel().getEndData().getValue();
            if (dt == null)
                dt = System.currentTimeMillis() / 1000;
            editDate(dt, false);
        });

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
        // TODO: fix blinking action
        inflater.inflate(R.menu.menu_edit_session, menu);
        MenuItem item = menu.findItem(R.id.menu_save);
        item.setEnabled(getViewModel().getIsChanged().getValue() == null || getViewModel().getIsChanged().getValue());

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
    protected EditSessionFragmentViewModel onCreateViewModel() {
        EditSessionFragmentViewModel vm = ViewModelProviders.of(this, mFactory).get(EditSessionFragmentViewModel.class);
        vm.getIsChanged().observe(this, aBoolean -> {
            if (getActivity() != null) getActivity().invalidateOptionsMenu();
        });
        return vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_session_edit;
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
        return "SessionEditFragment: id = " + getViewModel().getModel().getSessionId() +
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
        }
        else {
            getViewModel().getModel().setEndDate(e.year, e.month, e.dayOfMonth);
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
        DatePickerFragment f = DatePickerFragment.newInstante(eventTag, unixTime);
        f.show(getFragmentManager(), eventTag + "date");
    }


    private void editTime(long unixTime, boolean isStart) {
        if (getFragmentManager() == null)
            return;

        String eventTag = makeTimePickerTag(isStart);
        TimePickerFragment f = TimePickerFragment.newInstante(eventTag, unixTime);
        f.show(getFragmentManager(), eventTag + "time");
    }


    public void setSessionId(long sessionId) {
        getViewModel().getModel().setId(sessionId);
    }
}
