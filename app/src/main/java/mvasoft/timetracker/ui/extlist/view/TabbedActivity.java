package mvasoft.timetracker.ui.extlist.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;

import com.drextended.actionhandler.listener.ActionClickListener;

import org.joda.time.DateTime;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.databinding.ActivityTabbedBinding;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.common.PagerAdapter;
import mvasoft.timetracker.ui.editsession.view.EditSessionActivity;
import mvasoft.timetracker.ui.extlist.modelview.TabbedActivityViewModel;
import mvasoft.timetracker.ui.preferences.PreferencesActivity;
import mvasoft.timetracker.utils.DateTimeFormatters;

public class TabbedActivity extends BindingSupportActivity<ActivityTabbedBinding,
        TabbedActivityViewModel> implements ExSessionListFragment.ISessionListCallbacks {

    private final ActionClickListener mActionHandler = new TabbedActivityActionHandler();
    private final DateTimeFormatters mFormatter = new DateTimeFormatters();
    private ActionMode mActionMode;

    @Inject
    public ViewModelProvider.Factory mViewModelFactory ;

    @Inject
    public Lazy<DataRepository> mRepository;
    private PagerAdapter mPagerAdapter;
    private boolean mIsExpanded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBinding().setVariable(BR.actionHandler, mActionHandler);
        setSupportActionBar(getBinding().toolbar);
        initViewPager();
        getBinding().datePickerTitle.setText(mFormatter.formatDate(System.currentTimeMillis() / 1000));

        getBinding().calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                DateTime dt = new DateTime(year, month + 1, dayOfMonth, 0, 0, 0);
                long date = dt.getMillis() / 1000;
                Fragment fragment = mPagerAdapter.getFragment(getBinding().viewPager.getCurrentItem());
                if (fragment instanceof ExSessionListFragment)
                    ((ExSessionListFragment) fragment).setDate(date);
                getBinding().datePickerTitle.setText(mFormatter.formatDate(date));
            }
        });
        getBinding().appBarLayout.addOnOffsetChangedListener(
                (appBarLayout, verticalOffset) -> mIsExpanded = verticalOffset == 0);

        getBinding().datePickerButton.setOnClickListener(v -> {
            float rotation = mIsExpanded ? 0 : 180;
            ViewCompat.animate(getBinding().datePickerArrow).rotation(rotation).start();

            getBinding().appBarLayout.setExpanded(!mIsExpanded, true);
        });
        getBinding().toolbar.setElevation(40);
    }

    @Override
    protected TabbedActivityViewModel onCreateViewModel() {
        return ViewModelProviders.of(this, mViewModelFactory).get(TabbedActivityViewModel.class);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_tabbed;
    }

    @Override
    protected int getModelVariableId() {
        return BR.view_model;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        mActionMode = mode;
        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        mActionMode = null;
        super.onSupportActionModeFinished(mode);
    }

    private void initViewPager() {
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return ExSessionListFragment.newInstance(getBinding().calendarView.getDate() / 1000);
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return getString(R.string.caption_tabs_empty) + position;
            }
        };

        getBinding().viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mActionMode == null)
                    return;

                mActionMode.finish();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        getBinding().viewPager.setAdapter(mPagerAdapter);
//        getBinding().tabLayout.setupWithViewPager(getBinding().viewPager);
    }

    public void editSession(long sessionId) {
        Intent intent = new Intent(this, EditSessionActivity.class);
        intent.putExtras(EditSessionActivity.makeArgs(sessionId));
        startActivity(intent);
    }


    public static class TabbedActivityActionType {
        public static final String TOGGLE = "toggle";
    }

    private class TabbedActivityActionHandler implements ActionClickListener {

        @Override
        public void onActionClick(View view, String actionType, Object model) {
            switch (actionType) {
                case TabbedActivityActionType.TOGGLE:
                    actionToggle();
                    break;
            }
        }
    }

    private void actionToggle() {
        LiveData<DataRepository.ToggleSessionResult> toggleResult = getViewModel().toggleSession();
        toggleResult.observe(this, new Observer<DataRepository.ToggleSessionResult>() {
            @Override
            public void onChanged(@Nullable DataRepository.ToggleSessionResult toggleSessionResult) {
                if (toggleSessionResult != null) {
                    switch (toggleSessionResult) {
                        case tgs_Started:
                            Snackbar.make(getBinding().fab, R.string.msg_session_started, Snackbar.LENGTH_LONG).show();
                            break;

                        case tgs_Stopped:
                            Snackbar.make(getBinding().fab, R.string.msg_session_stopped, Snackbar.LENGTH_LONG).show();
                            break;
                    }
                }

            }
        });

    }
}
