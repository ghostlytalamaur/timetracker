package mvasoft.timetracker.ui.extlist.view;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

import com.drextended.actionhandler.listener.ActionClickListener;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.common.FileUtils;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.databinding.ActivityTabbedBinding;
import mvasoft.timetracker.db.DatabaseHelper;
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

    private static final int PERMISSION_REQUEST_STORAGE = 1;
    private static final int ACTIVITY_REQUEST_IMPORT_DB = 1;
    private static final String STATE_DATE = "selected_date";
    private static final String STATE_TAB = "tab_num";
    private static final String PREF_DATE = "selected_date";
    private static final String PREF_TAB  = "tab_num";


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
        restoreState(savedInstanceState);
    }

    private void restoreState(Bundle savedInstanceState) {
        int currentTab = 0;
        if (savedInstanceState != null) {
            mDate = savedInstanceState.getLong(STATE_DATE, System.currentTimeMillis() / 1000);
            currentTab = savedInstanceState.getInt(STATE_TAB, currentTab);
        }
        else {
            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            mDate = pref.getLong(PREF_DATE, getBinding().calendarView.getDate() / 1000);
            currentTab = pref.getInt(PREF_TAB, currentTab);
        }
        getBinding().calendarView.setDate(mDate * 1000);
        getBinding().datePickerTitle.setText(mFormatter.formatDate(mDate));
        getBinding().viewPager.setCurrentItem(currentTab);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_DATE, mDate);
        outState.putInt(STATE_TAB, getBinding().viewPager.getCurrentItem());
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        pref.edit().putLong(PREF_DATE, mDate)
                .putInt(PREF_TAB, getBinding().viewPager.getCurrentItem())
                .apply();
    }

    private void updateFragmentDate() {
        Fragment fragment = mPagerAdapter.getFragment(getBinding().viewPager.getCurrentItem());
        if (fragment instanceof ExSessionListFragment) {
            ExSessionListFragment listFragment = ((ExSessionListFragment) fragment);
            Pair<Long, Long> dateRange = getDateForFragment(getBinding().viewPager.getCurrentItem());
            //noinspection ConstantConditions
            listFragment.setDate(dateRange.first, dateRange.second);
        }
    }

    @NonNull
    private Pair<Long, Long> getDateForFragment(int position) {
        switch (position) {
            case 1: // Week
                return new Pair<>(DateTimeHelper.startOfWeek(mDate), DateTimeHelper.endOfWeek(mDate));
            case 2: // Month
                return new Pair<>(DateTimeHelper.startOfMonth(mDate), DateTimeHelper.endOfMonth(mDate));
            default: // Day
                return new Pair<>(mDate, mDate);
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            case R.id.action_edit_date:
                intent = new Intent(this, EditDateActivity.class);
                intent.putExtras(EditDateActivity.makeArgs(mDate));
                startActivity(intent);
            case R.id.action_import_db_old:
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    selectDbToImport();
                else
                    Toast.makeText(this,
                            "Cannot perform operation without appropriate permission",
                            Toast.LENGTH_LONG).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkPermission(@NonNull String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[] {permission}, requestCode);
    }

    private void selectDbToImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent = Intent.createChooser(intent, "Select db");
        startActivityForResult(intent, ACTIVITY_REQUEST_IMPORT_DB);
    }

    private void importOldDb(Uri uri) {
        File file = null;
        try {
            file = FileUtils.createTempFileInputStream(getContentResolver().openInputStream(uri));
            DatabaseHelper.importOldDb(file, mRepository.get());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (file != null)
                //noinspection ResultOfMethodCallIgnored
                file.delete();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_REQUEST_IMPORT_DB:
                if (resultCode == RESULT_OK && data != null && data.getData() != null)
                    importOldDb(data.getData());
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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
