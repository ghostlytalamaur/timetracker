package mvasoft.timetracker.ui.editsession.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.drextended.actionhandler.listener.ActionClickListener;

import java.util.List;

import javax.inject.Inject;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.ActivityEditSessionBinding;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.common.PagerAdapter;
import mvasoft.timetracker.ui.editsession.viewmodel.EditSessionActivityViewModel;

public class EditSessionActivity extends BindingSupportActivity<ActivityEditSessionBinding,
        EditSessionActivityViewModel> implements ActionClickListener {

    private static final String ARGS_START_ID = "ARGS_START_ID";
    private static final String STATE_CURRENT_ID = "STATE_CURRENT_ID";

    private long mCurrentId;
    private PagerAdapter mAdapter;
    private LiveData<List<Long>> mSessionIds;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    public static Bundle makeArgs(long startId) {
        Bundle b = new Bundle();
        b.putLong(ARGS_START_ID, startId);
        return b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentId = -1;
        if (savedInstanceState != null)
            mCurrentId = savedInstanceState.getLong(STATE_CURRENT_ID);
        else {
            Bundle extras = getIntent().getExtras();
            if (extras != null)
                mCurrentId = extras.getLong(ARGS_START_ID, mCurrentId);
        }

        setSupportActionBar(getBinding().toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSessionIds = getViewModel().getSessionsIds();
        mSessionIds.observe(this, (data) -> onIdListChanged());
        getBinding().setVariable(BR.actionHandler, this);
        initViewPager();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentId = savedInstanceState.getLong(STATE_CURRENT_ID, mCurrentId);
    }

    private void onIdListChanged() {
        mAdapter.notifyDataSetChanged();
        if (mSessionIds.getValue() != null) {
            int newPos = mSessionIds.getValue().indexOf(mCurrentId);
            if (newPos > 0 && getBinding().viewPager.getCurrentItem() != newPos) {
                getBinding().viewPager.setCurrentItem(newPos);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_session;
    }

    @Override
    protected EditSessionActivityViewModel getViewModel() {
        return ViewModelProviders.of(this, mViewModelFactory).get(EditSessionActivityViewModel.class);
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
        getBinding().viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mSessionIds.getValue() != null)
                    mCurrentId = mSessionIds.getValue().get(position);
                SessionEditFragment fragment = (SessionEditFragment) mAdapter.getFragment(position);
                if (fragment != null)
                    fragment.setSessionId(mCurrentId);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mAdapter = new PagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                if (mSessionIds.getValue() != null)
                    return SessionEditFragment.newInstance(mSessionIds.getValue().get(position));
                else
                    return null;
            }

            @Override
            public int getCount() {
                if (mSessionIds.getValue() != null)
                    return mSessionIds.getValue().size();
                else
                    return 0;
            }
        };
        getBinding().viewPager.setAdapter(mAdapter);
    }

    @Override
    public void onActionClick(View view, String actionType, Object model) {
        // TODO: remove if not needed
        switch (actionType) {
            case EditSessionActionType.SAVE:
                break;
        }
    }

    public abstract static class EditSessionActionType {
        static final String SAVE = "safe";
    }
}
