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
import mvasoft.timetracker.ui.editdate.view.EditDateActivity;
import mvasoft.timetracker.ui.editsession.view.EditSessionActivity;
import mvasoft.timetracker.ui.extlist.modelview.TabbedActivityViewModel;
import mvasoft.timetracker.ui.preferences.PreferencesActivity;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.utils.DateTimeHelper;

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
    private long mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBinding().setVariable(BR.actionHandler, mActionHandler);
        setSupportActionBar(getBinding().toolbar);
        initViewPager();
        getBinding().datePickerTitle.setText(mFormatter.formatDate(System.currentTimeMillis() / 1000));

        // TODO: save selected date in savedInstanceState
        mDate = getBinding().calendarView.getDate() / 1000;
        getBinding().calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                DateTime dt = new DateTime(year, month + 1, dayOfMonth, 0, 0, 0);
                mDate = dt.getMillis() / 1000;
                updateFragmentDate();
                getBinding().datePickerTitle.setText(mFormatter.formatDate(mDate));
            }
        });
        getBinding().appBarLayout.addOnOffsetChangedListener(
                (appBarLayout, verticalOffset) -> mIsExpanded = verticalOffset == 0);

        getBinding().datePickerButton.setOnClickListener(v -> {
            float rotation = mIsExpanded ? 0 : 180;
            ViewCompat.animate(getBinding().datePickerArrow).rotation(rotation).start();

            getBinding().appBarLayout.setExpanded(!mIsExpanded, true);
        });
    }

    private void updateFragmentDate() {
        Fragment fragment = mPagerAdapter.getFragment(getBinding().viewPager.getCurrentItem());
        if (fragment instanceof ExSessionListFragment) {
            ExSessionListFragment listFragment = ((ExSessionListFragment) fragment);
            switch (getBinding().viewPager.getCurrentItem()) {
                case 1: // Week
                    listFragment.setDate(DateTimeHelper.startOfWeek(mDate), DateTimeHelper.endOfWeek(mDate));
                    break;
                case 2: // Month
                    listFragment.setDate(DateTimeHelper.startOfMonth(mDate), DateTimeHelper.endOfMonth(mDate));
                    break;
                default: // Day
                    listFragment.setDate(mDate, mDate);
                    break;
            }
        }
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
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            case R.id.action_edit_date:
                intent = new Intent(this, EditDateActivity.class);
                intent.putExtras(EditDateActivity.makeArgs(mDate));
                startActivity(intent);
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
                // TODO: set date properly
                return ExSessionListFragment.newInstance(getBinding().calendarView.getDate() / 1000);
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0: return getString(R.string.caption_tabs_day);
                    case 1: return getString(R.string.caption_tabs_week);
                    case 2: return getString(R.string.caption_tabs_month);
                    default: return "Undefined";
                }
            }
        };

        getBinding().viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateFragmentDate();
                if (mActionMode == null)
                    return;

                mActionMode.finish();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        getBinding().viewPager.setAdapter(mPagerAdapter);
        getBinding().tabLayout.setupWithViewPager(getBinding().viewPager);
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
        // TODO: move fab action handler to fragment
        final LiveData<DataRepository.ToggleSessionResult> toggleResult = getViewModel().toggleSession();
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
                toggleResult.removeObserver(this);
            }
        });

    }
}
