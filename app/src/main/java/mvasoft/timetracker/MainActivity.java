package mvasoft.timetracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Timer;
import java.util.TimerTask;

import mvasoft.timetracker.GroupsList.GroupType;
import mvasoft.timetracker.data.DatabaseDescription;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int GROUPS_LOADER_ID   = 1;
    private static final int TODAY_LOADER_ID    = 2;
    private static final int WEEK_LOADER_ID     = 3;
    private static final long EMPTY_ID = -1;
    private SessionsAdapter mAdapter;
    private FloatingActionButton mFab;
    private TextView mTodayTv;
    private TextView mWeekTv;

    private PeriodFormatter mPeriodFormatter;
    private GroupType mCurrentGroupType;
    private GroupsList mCurrentGroups;
    private GroupsList mTodayGroup;
    private GroupsList mWeekGroup;

    private Handler mHandler;
    private Timer mUpdateTimer;
    private Runnable mUpdateViewRunnable;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    switch (id) {
                        case GROUPS_LOADER_ID:
                            return new CursorLoader(MainActivity.this,
                                    getCurrentGroupsUri(),
                                    null, null, null,
                                    DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
                        case TODAY_LOADER_ID:
                            return new CursorLoader(MainActivity.this,
                                    DatabaseDescription.GroupsDescription.GROUP_DAY_URI,
                                    null,
                                    String.format("date(%1$s, 'unixepoch') = date('now')",
                                            DatabaseDescription.SessionDescription.COLUMN_START),
                                    null,
                                    DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
                        case WEEK_LOADER_ID:
                            return new CursorLoader(MainActivity.this,
                                    DatabaseDescription.GroupsDescription.GROUP_WEEK_URI,
                                    null,
                                    String.format(
                                            "date(%1$s, 'unixepoch', 'weekday 0', '-7 days')" +
                                                    "= date('now', 'unixepoch', 'weekday 0', '-7 days')",
                                            DatabaseDescription.SessionDescription.COLUMN_START),
                                    null,
                                    DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
                        default:
                            return null;
                    }
                }

                private Uri getCurrentGroupsUri() {
                    switch (mCurrentGroupType) {
                        case gt_None:
                            return DatabaseDescription.GroupsDescription.GROUP_NONE_URI;
                        case gt_Day:
                            return DatabaseDescription.GroupsDescription.GROUP_DAY_URI;
                        case gt_Week:
                            return DatabaseDescription.GroupsDescription.GROUP_WEEK_URI;
                        case gt_Month:
                            return DatabaseDescription.GroupsDescription.GROUP_MONTH_URI;
                        case gt_Year:
                            return DatabaseDescription.GroupsDescription.GROUP_YEAR_URI;
                    }

                    return null;
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    swapCursor(loader.getId(), data);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    swapCursor(loader.getId(), null);
                }

                private void swapCursor(int id, Cursor cursor) {
                    switch (id) {
                        case GROUPS_LOADER_ID: {
                            swapCurrentGroupCursor(cursor);
                            break;
                        }
                        case TODAY_LOADER_ID: {
                            swapTodayCursor(cursor);
                        }
                        case WEEK_LOADER_ID: {
                            swapWeekCursor(cursor);
                            break;
                        }
                    }
                }

                private void swapCurrentGroupCursor(Cursor cursor) {
                    mCurrentGroups.swapCursor(cursor);
                    updateFabIcon();
                }

                private void swapTodayCursor(Cursor cursor) {
                    mTodayGroup.swapCursor(cursor);
                    updateTimeText();
                }

                private void swapWeekCursor(Cursor cursor) {
                    mWeekGroup.swapCursor(cursor);
                    updateTimeText();
                }
            };

    private class FabOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            toggleSession();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPeriodFormatter = new PeriodFormatterBuilder().
                printZeroAlways().
                minimumPrintedDigits(2).
                appendHours().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendMinutes().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendSeconds().
                toFormatter();


        mTodayTv = (TextView) findViewById(R.id.tvDay);
        mWeekTv = (TextView) findViewById(R.id.tvWeek);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new FabOnClickListener());


        mCurrentGroupType = GroupType.values()[getPreferences(MODE_PRIVATE).
                getInt(PreferencesConst.GROUP_TYPE, GroupType.gt_None.ordinal())];


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mTodayGroup = new GroupsList();
        mWeekGroup = new GroupsList();
        mCurrentGroups = new GroupsList();
        mAdapter = new SessionsAdapter();
        mAdapter.setList(mCurrentGroups);

        RecyclerView itemsView = (RecyclerView) findViewById(R.id.items_view);
        itemsView.addItemDecoration(new DividerItemDecoration(itemsView.getContext(),
                DividerItemDecoration.VERTICAL));
        itemsView.setLayoutManager(new LinearLayoutManager(this));
        itemsView.setAdapter(mAdapter);

        mUpdateViewRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeText();
            }
        };
        mHandler = new Handler();
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mUpdateViewRunnable);
            }
        }, 1000, 1000);


        getSupportLoaderManager().initLoader(GROUPS_LOADER_ID, null, mLoaderCallbacks);
        getSupportLoaderManager().initLoader(TODAY_LOADER_ID, null, mLoaderCallbacks);
    }

    private void setCurrentGroupType(GroupType type) {
        mCurrentGroupType = type;
        getPreferences(MODE_PRIVATE).edit().
                putInt(PreferencesConst.GROUP_TYPE, type.ordinal()).apply();
        getSupportLoaderManager().restartLoader(GROUPS_LOADER_ID, null, mLoaderCallbacks);
    }

    private void updateTimeText() {
        mAdapter.updateNotClosedView();
        updateCurrenDayText(mTodayTv, mTodayGroup, R.string.text_today, R.string.text_today_empty);
        updateCurrenDayText(mWeekTv, mWeekGroup, R.string.text_week, R.string.text_week_empty);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        MenuItem item = menu.findItem(R.id.action_group_type);
        Spinner spGroups = (Spinner) item.getActionView();

        final ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(this, R.array.group_types,
                R.layout.spinner_item);
        spAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGroups.setAdapter(spAdapter);
        spGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setCurrentGroupType(GroupType.values()[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setCurrentGroupType(GroupType.gt_None);
            }
        });
        spGroups.setSelection(mCurrentGroupType.ordinal());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean startNewSession() {
        ContentValues values = new ContentValues();
        values.put(DatabaseDescription.SessionDescription.COLUMN_START,
                System.currentTimeMillis() / 1000L);
        return getContentResolver().insert(DatabaseDescription.SessionDescription.CONTENT_URI, values) != null;
    }

    private boolean stopSession() {
        long id = getOpenedSessionID();
        if (id == EMPTY_ID)
            return false;

        ContentValues values = new ContentValues();
        values.put(DatabaseDescription.SessionDescription.COLUMN_END, System.currentTimeMillis() / 1000L);

        return getContentResolver().update(DatabaseDescription.SessionDescription.buildSessionUri(id),
                values, null, null) > 0;
    }

    private void toggleSession() {
        if (stopSession())
            Snackbar.make(mFab, "SessionDescription stopped.", Snackbar.LENGTH_LONG).show();
        else if (startNewSession())
            Snackbar.make(mFab, "SessionDescription started.", Snackbar.LENGTH_LONG).show();

        updateFabIcon();
    }

    private long getOpenedSessionID() {
        Cursor cursor = getContentResolver().query(DatabaseDescription.SessionDescription.CONTENT_URI, null,
                DatabaseDescription.SessionDescription.COLUMN_END + " is null", null, null);
        if ((cursor == null) || (cursor.getCount() <= 0))
                return EMPTY_ID;

        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));
        cursor.close();
        return id;
    }

    private void updateFabIcon() {
        if (mCurrentGroups.hasOpenedSessions())
            mFab.setImageResource(android.R.drawable.ic_media_pause);
        else
            mFab.setImageResource(android.R.drawable.ic_media_play);
    }

    private void updateCurrenDayText(TextView v, GroupsList groups, int textId, int emptyTextId) {
        long elapsed = groups.getDuration();
        if (elapsed > 0)
            v.setText(String.format(getString(textId),
                    mPeriodFormatter.print(new Period(elapsed * 1000L))));
        else
            v.setText(getString(emptyTextId));
    }

}
