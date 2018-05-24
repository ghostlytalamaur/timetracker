package mvasoft.timetracker.ui.editsession.view;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
    private SessionsPagerAdapter mAdapter;
    private long mInitSessionId;

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
        // if has savedInstanceState, then current tab will be restored
        // by internal viewPager implementation
        mInitSessionId = -1;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mInitSessionId = extras.getLong(ARGS_START_ID, mInitSessionId);
            }
        }

        setSupportActionBar(getBinding().toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new SessionsPagerAdapter(getSupportFragmentManager());

        getBinding().viewPager.setAdapter(mAdapter);
        getBinding().tabLayout.setupWithViewPager(getBinding().viewPager);
        getViewModel().getSessionsIds().observe(this, this::onIdListChanged);
    }

    private void setCurrentId(long id) {
        if (id < 0)
            return;

        List<Long> ids = getViewModel().getSessionsIds().getValue();
        if (ids == null)
            return;

        int newPos = ids.indexOf(id);
        if (newPos > 0 && getBinding().viewPager.getCurrentItem() != newPos) {
            getBinding().viewPager.setCurrentItem(newPos, false);
        }
    }

    private void onIdListChanged(List<Long> ids) {
        mAdapter.setSessionIds(ids);
        if (ids != null && mInitSessionId > 0) {
            setCurrentId(mInitSessionId);
            mInitSessionId = -1;
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
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionSavedEvent(SessionSavedEvent e) {
        if (e.wasSaved)
            Toast.makeText(this, R.string.session_saved, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, R.string.session_unable_save, Toast.LENGTH_SHORT).show();
    }

    private static class SessionsPagerAdapter extends PagerAdapter {
        private List<Long> mIds;

        SessionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mIds != null ? String.valueOf(mIds.get(position)) : "";
        }

        @Override
        public Fragment getItem(int position) {
            return mIds != null ? EditSessionFragment.newInstance(mIds.get(position)) : null;
        }

        @Override
        public int getCount() {
            return mIds != null ? mIds.size() : 0;
        }

        @Override
        public long getItemId(int position) {
            return mIds != null ? mIds.get(position) : -1;
        }

        @Override
        protected long getItemId(@NonNull Object object) {
            if (!(object instanceof EditSessionFragment))
                return super.getItemId(object);
            else
                return ((EditSessionFragment) object).getSessionId();
        }

        @Override
        protected boolean hasIds() {
            return true;
        }

        void setSessionIds(List<Long> ids) {
            mIds = ids;
            notifyDataSetChanged();
        }
    }

}
