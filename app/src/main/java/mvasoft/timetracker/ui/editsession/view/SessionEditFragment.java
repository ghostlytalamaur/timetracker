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

import javax.inject.Inject;

import mvasoft.datetimepicker.DateTimePickerFragment;
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
                        getViewModel().getModel().setStartTime(newDateTime / 1000);
                    break;
                case REQUEST_END_TIME:
                    newDateTime = data.getLongExtra(DateTimePickerFragment.ARGS_DATE, -1);
                    if (newDateTime > 0)
                        getViewModel().getModel().setEndTime(newDateTime / 1000);
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
        getBinding().tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long dt = getViewModel().getModel().getStartData().getValue();
                if (dt == null)
                    dt = System.currentTimeMillis() / 1000;

                editDateTime(dt, REQUEST_START_TIME);
            }
        });

        getBinding().tvEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long dt = getViewModel().getModel().getEndData().getValue();
                if (dt == null)
                    dt = System.currentTimeMillis() / 1000;

                editDateTime(dt, REQUEST_END_TIME);
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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

    private void editDateTime(long dateTime, int requestCode) {
        if (getFragmentManager() == null)
            return;

        if (dateTime == 0)
            dateTime = System.currentTimeMillis() / 1000L;
        DateTimePickerFragment dlg = DateTimePickerFragment.newInstance(dateTime * 1000, "");
        dlg.setTargetFragment(this, requestCode);
        dlg.show(getFragmentManager(), "dialog_date");
    }

    public void setSessionId(long sessionId) {
        getViewModel().getModel().setId(sessionId);
    }
}
