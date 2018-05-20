package mvasoft.timetracker.ui.editsession.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.data.event.SessionSavedEvent;
import mvasoft.timetracker.databinding.ActivityEditSessionBinding;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.common.PagerAdapter;
import mvasoft.timetracker.ui.editsession.viewmodel.EditSessionActivityViewModel;

public class EditSessionActivity extends BindingSupportActivity<ActivityEditSessionBinding,
        EditSessionActivityViewModel> {

    private static final String ARGS_START_ID = "ARGS_START_ID";
    private static final String STATE_CURRENT_ID = "STATE_CURRENT_ID";

    private long mCurrentId;
    private PagerAdapter mAdapter;
    private LiveData<List<Long>> mSessionIds;
    private List<Long> mPreviousIds;

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
                getBinding().viewPager.setCurrentItem(newPos, false);
            }
        }

        // store ids to calculate position changes on next call in adapter
        mPreviousIds = mSessionIds.getValue();
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

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (mSessionIds.getValue() != null)
                    return String.valueOf(mSessionIds.getValue().get(position));
                else
                    return "";
            }

            @Override
            public long getItemId(int position) {
                return mSessionIds.getValue() != null ? mSessionIds.getValue().get(position) : -1;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                if (!(object instanceof SessionEditFragment) || (mSessionIds.getValue() == null))
                    return super.getItemPosition(object);

                SessionEditFragment f = (SessionEditFragment) object;
                long sessionId = f.getSessionId();
                int newIdx = mSessionIds.getValue().indexOf(sessionId);
                if (newIdx < 0)
                    return POSITION_NONE;

                int oldIdx = (mPreviousIds != null ? mPreviousIds.indexOf(sessionId) : -1);
                if (oldIdx == newIdx)
                    return POSITION_UNCHANGED;
                else
                    return newIdx;
            }

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
        getBinding().tabLayout.setupWithViewPager(getBinding().viewPager);
    }

    @Override
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionSavedEvent(SessionSavedEvent e) {
        if (e.wasSaved)
            Toast.makeText(this,  R.string.session_saved, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,  R.string.session_unable_save, Toast.LENGTH_SHORT).show();
        Log.d("mvasoft.timetracker.log", "EditSessionActivity.onSessionSavedEvent(SessionSavedEvent e)");
    }

}
