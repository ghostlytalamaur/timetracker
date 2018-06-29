package mvasoft.timetracker.ui.extlist;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import javax.inject.Inject;

import mvasoft.dialogs.DatePickerFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.data.event.SessionToggledEvent;
import mvasoft.timetracker.data.event.SessionsDeletedEvent;
import mvasoft.timetracker.databinding.ActivityTabbedBinding;
import mvasoft.timetracker.ui.backup.BackupActivity;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.common.PagerAdapter;
import mvasoft.timetracker.ui.editdate.EditDateActivity;
import mvasoft.timetracker.ui.preferences.PreferencesActivity;
import mvasoft.timetracker.utils.DateTimeHelper;

public class TabbedActivity extends BindingSupportActivity<ActivityTabbedBinding,
        TabbedActivityViewModel> implements DialogResultListener,
        ExSessionListFragment.VisibleFragmentInfoProvider {

    private static final String DATE_PICKER_TAG = "TabbedActivitySelectDateDlg";
    private static final int DLG_REQUEST_DATE = 1;

    @Inject
    public ViewModelProvider.Factory mViewModelFactory;
    private ActionMode mActionMode;
    private PagerAdapter mPagerAdapter;

    @Override
    public boolean isFragmentVisible(Fragment fragment) {
        return mPagerAdapter.getFragment(getBinding().viewPager.getCurrentItem()) == fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(getBinding().toolbar);
        initViewPager();

        getBinding().fab.setOnClickListener(v -> getViewModel().toggleSession());
        getViewModel().getDate().observe(this, date -> updateFragmentDate());
        getViewModel().getTitleData().observe(this, this::setTitle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getViewModel().saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getViewModel().restoreState(savedInstanceState);
    }

    private void updateFragmentDate() {
        Fragment fragment = mPagerAdapter.getFragment(getBinding().viewPager.getCurrentItem());
        if (fragment instanceof ExSessionListFragment) {
            ExSessionListFragment listFragment = ((ExSessionListFragment) fragment);
            Pair<Long, Long> dateRange = getDateForFragment(getBinding().viewPager.getCurrentItem());
            //noinspection ConstantConditions
            listFragment.setDate(dateRange.first, dateRange.second);

            listFragment.updateActionMode();
        }
    }

    @NonNull
    private Pair<Long, Long> getDateForFragment(int position) {
        long date = Objects.requireNonNull(getViewModel().getDate().getValue());
        switch (position) {
            case 1: // Week
                return new Pair<>(
                        DateTimeHelper.startOfMonthWeek(date),
                        DateTimeHelper.endOfMonthWeek(date));
            case 2: // Month
                return new Pair<>(
                        DateTimeHelper.startOfMonth(date),
                        DateTimeHelper.endOfMonth(date));
            default: // Day
                return new Pair<>(date, date);
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
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_edit_date: {
                Intent intent = new Intent(this, EditDateActivity.class);
                intent.putExtras(EditDateActivity.makeArgs(
                        Objects.requireNonNull(getViewModel().getDate().getValue())));
                startActivity(intent);
                break;
            }
            case R.id.action_import_export: {
                Intent intent = new Intent(this, BackupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            }
            case R.id.action_select_date:
                selectDate();
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
                Pair<Long, Long> dateRange = getDateForFragment(position);
                //noinspection ConstantConditions
                return ExSessionListFragment.newInstance(dateRange.first, dateRange.second);
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.caption_tabs_day);
                    case 1:
                        return getString(R.string.caption_tabs_week);
                    case 2:
                        return getString(R.string.caption_tabs_month);
                    default:
                        return "Undefined";
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
//                if (mActionMode == null)
//                    return;

//                mActionMode.finish();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        getBinding().viewPager.setAdapter(mPagerAdapter);
        getBinding().tabLayout.setupWithViewPager(getBinding().viewPager);
    }

    @Override
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionToggled(SessionToggledEvent e) {
        switch (e.toggleResult) {
            case tgs_Started:
                Snackbar.make(getBinding().fab, R.string.msg_session_started, Snackbar.LENGTH_LONG).show();
                break;

            case tgs_Stopped:
                Snackbar.make(getBinding().fab, R.string.msg_session_stopped, Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionsDeletedEvent(SessionsDeletedEvent e) {
        Toast.makeText(this, e.deletedSessionsCount + " session was removed", Toast.LENGTH_LONG).show();
    }

    public void onDateSelected(DatePickerFragment.DatePickerDialogResultData data) {
        getViewModel().setDate(data.year, data.month, data.dayOfMonth);
    }

    private void selectDate() {
        new DatePickerFragment.Builder(DLG_REQUEST_DATE)
                .withUnixTime(Objects.requireNonNull(getViewModel().getDate().getValue()))
                .show(this, DATE_PICKER_TAG);
    }

    @Override
    public void onDialogResult(@NonNull DialogResultData data) {
        switch (data.requestCode) {
            case DLG_REQUEST_DATE:
                onDateSelected((DatePickerFragment.DatePickerDialogResultData) data);
                break;
        }
    }
}
