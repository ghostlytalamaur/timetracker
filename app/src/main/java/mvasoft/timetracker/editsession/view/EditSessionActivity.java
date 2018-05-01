package mvasoft.timetracker.editsession.view;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.drextended.actionhandler.listener.ActionClickListener;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.SessionEditFragment;
import mvasoft.timetracker.databinding.ActivityEditSessionBinding;
import mvasoft.timetracker.editsession.viewmodel.EditSessionViewModel;
import mvasoft.timetracker.ui.base.BindingSupportActivity;

public class EditSessionActivity extends BindingSupportActivity<ActivityEditSessionBinding,
        EditSessionViewModel> implements ActionClickListener {

    private static final String ARGS_START_ID = "ARGS_START_ID";

    private long mCurrentId;

    public static Bundle makeArgs(long startId) {
        Bundle b = new Bundle();
        b.putLong(ARGS_START_ID, startId);
        return b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentId = -1;
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            mCurrentId = extras.getLong(ARGS_START_ID, mCurrentId);

        getBinding().setVariable(BR.actionHandler, this);
        initViewPager();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_session;
    }

    @Override
    protected EditSessionViewModel getViewModel() {
        return ViewModelProviders.of(this).get(EditSessionViewModel.class);
    }

    @Override
    protected int getModelVariableId() {
        return BR.view_model;
    }

    @Override
    protected void bindVariables() {
        super.bindVariables();
        getBinding().setVariable(BR.actionHandler, this);
    }

    private void initViewPager() {
        getBinding().viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return SessionEditFragment.newInstance(mCurrentId);
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
    }

    @Override
    public void onActionClick(View view, String actionType, Object model) {
        switch (actionType) {
            case EditSessionActionType.SAVE:
                saveSession();
                break;
        }
    }

    private void saveSession() {
//        getBinding().viewPager.getAdapter().
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.);
//        if (!(fragment instanceof SessionEditFragment))
//            return;
//
//        ((SessionEditFragment) fragment).saveSession();
    }
}
