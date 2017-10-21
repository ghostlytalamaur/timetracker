package mvasoft.timetracker;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.ItemSelectionSupport;

import java.util.Timer;
import java.util.TimerTask;


import static android.content.Context.MODE_PRIVATE;


public class SessionListFragment extends FabFragment {

    private SessionsAdapter mAdapter;
    private TextView mTodayTv;
    private TextView mWeekTv;

    private PeriodFormatter mPeriodFormatter;
    private GroupsList mCurrentGroups;
    private GroupsList mTodayGroup;
    private GroupsList mWeekGroup;

    private Handler mHandler;
    private Timer mUpdateTimer;
    private Runnable mUpdateViewRunnable;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks;
    private ItemSelectionSupport mSelectionSupport;
    private GroupInfoProvider mGroupInfoProvider;
    private ActionMode.Callback mActionModeCallbacks;
    private ActionMode mActionMode;
    private SessionHelper mSessionHelper;
    private FloatingActionButton mFab;

    public static SessionListFragment newInstance() {
        return new SessionListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mGroupInfoProvider = new GroupInfoProvider();
        mGroupInfoProvider.setCurrentGroupType(
                GroupType.values()[getActivity().getPreferences(MODE_PRIVATE).
                getInt(PreferencesConst.GROUP_TYPE, GroupType.gt_None.ordinal())]
        );

        mSessionHelper = new SessionHelper(getActivity());
        mTodayGroup = new GroupsList();
        mWeekGroup = new GroupsList();
        mCurrentGroups = new GroupsList();
        GroupsList.IGroupsChangesListener groupListener = new GroupsChangesListener();
        mCurrentGroups.addChangesListener(groupListener);

        mAdapter = new SessionsAdapter();
        mAdapter.setList(mCurrentGroups);

        mActionModeCallbacks = new ActionModeCallbacks();

        mUpdateViewRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeText();
            }
        };
        mHandler = new Handler();
        mUpdateTimer = new Timer();

        mLoaderCallbacks = new GroupsLoaderCallbacks(getContext(), mGroupInfoProvider,
                mCurrentGroups, mTodayGroup, mWeekGroup);

        LoaderManager lm = getActivity().getSupportLoaderManager();
        lm.initLoader(GroupsLoaderCallbacks.WEEK_LOADER_ID, null, mLoaderCallbacks);
        lm.initLoader(GroupsLoaderCallbacks.GROUPS_LOADER_ID, null, mLoaderCallbacks);
        lm.initLoader(GroupsLoaderCallbacks.TODAY_LOADER_ID,null, mLoaderCallbacks);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_session_list, container, false);

        mTodayTv = fragmentView.findViewById(R.id.tvDay);
        mWeekTv = fragmentView.findViewById(R.id.tvWeek);
        mFab = fragmentView.findViewById(R.id.fab);
        mFab.setOnClickListener(new FabClickListener());
        updateFabIcon();

        final RecyclerView itemsView = fragmentView.findViewById(R.id.items_view);
        itemsView.setAdapter(mAdapter);
        itemsView.setLayoutManager(new LinearLayoutManager(itemsView.getContext()));
        itemsView.addItemDecoration(new DividerItemDecoration(itemsView.getContext(),
                DividerItemDecoration.VERTICAL));
        mSelectionSupport = ItemSelectionSupport.addTo(itemsView);
        mAdapter.setItemSelection(mSelectionSupport);

        SessionClickListener sessionClickListener = new SessionClickListener();
        ItemClickSupport clickSupport = ItemClickSupport.addTo(itemsView);
        clickSupport.setOnItemClickListener(sessionClickListener);
        clickSupport.setOnItemLongClickListener(sessionClickListener);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mUpdateViewRunnable);
            }
        }, 1000, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mUpdateTimer.cancel();
        mUpdateTimer.purge();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_group_type);
        Spinner spGroups = (Spinner) item.getActionView();

        final ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.group_types, R.layout.spinner_item);
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
        spGroups.setSelection(mGroupInfoProvider.getCurrentGroupType().ordinal());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_fill_fake_sessions: {
                FakeSessionFiller.fill(getContext());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCurrentGroupType(GroupType type) {
        mGroupInfoProvider.setCurrentGroupType(type);
        getActivity().getPreferences(MODE_PRIVATE).edit().
                putInt(PreferencesConst.GROUP_TYPE, type.ordinal()).apply();
        getActivity().getSupportLoaderManager().restartLoader(
                GroupsLoaderCallbacks.GROUPS_LOADER_ID, null, mLoaderCallbacks);
    }

    private void updateTimeText() {
        mAdapter.updateNotClosedView();
        updateCurrentDayText(mTodayTv, mTodayGroup, R.string.text_today, R.string.text_today_empty);
        updateCurrentDayText(mWeekTv, mWeekGroup, R.string.text_week, R.string.text_week_empty);
    }

    private void updateCurrentDayText(@NonNull TextView v, GroupsList groups, @StringRes int textId,
                                      @StringRes int emptyTextId) {
        long elapsed = groups.getDuration();
        if (elapsed > 0)
            v.setText(String.format(getString(textId),
                    mPeriodFormatter.print(new Period(elapsed * 1000L))));
        else
            v.setText(getString(emptyTextId));
    }

    private void updateActionModeTitle() {
        if (mActionMode == null)
            return;

        mActionMode.setTitle(String.format(getString(R.string.title_session_selected),
                mSelectionSupport.getCheckedItemCount()));

    }

    private void updateFabIcon() {
        if (mCurrentGroups.hasOpenedSessions())
            mFab.setImageResource(android.R.drawable.ic_media_pause);
        else
            mFab.setImageResource(android.R.drawable.ic_media_play);
    }

    private class SessionClickListener implements ItemClickSupport.OnItemClickListener,
            ItemClickSupport.OnItemLongClickListener {
        @Override
        public void onItemClick(RecyclerView parent, View view, int position, long id) {
            updateActionModeTitle();
        }

        @Override
        public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {
            if (mActionMode != null)
                return false;

            ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallbacks);
            mSelectionSupport.setItemChecked(position, true);
            updateActionModeTitle();
            return true;
        }
    }

    private class ActionModeCallbacks implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.action_mode, menu);
            mFab.setVisibility(View.INVISIBLE);
            mSelectionSupport.setChoiceMode(ItemSelectionSupport.ChoiceMode.MULTIPLE);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean res = false;
            switch (item.getItemId()) {
                case R.id.action_delete_selected:
                    res = mSessionHelper.deleteGroups(mGroupInfoProvider.getCurrentGroupsUri(),
                            mSelectionSupport.getCheckedItemIds());
            }
            if (res)
                mActionMode.finish();
            return res;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mSelectionSupport.clearChoices();
            mSelectionSupport.setChoiceMode(ItemSelectionSupport.ChoiceMode.NONE);
            mFab.setVisibility(View.VISIBLE);
        }

    }

    private class FabClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            SessionHelper.ToggleSessionResult toggleResult = mSessionHelper.toggleSession();
            switch (toggleResult) {
                case tgs_Started:
                    Snackbar.make(mFab, "SessionDescription stopped.", Snackbar.LENGTH_LONG).show();
                    break;

                case tgs_Stopped:
                    Snackbar.make(mFab, "SessionDescription started.", Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private class GroupsChangesListener implements GroupsList.IGroupsChangesListener {
        @Override
        public void onDataChanged() {
            updateFabIcon();
            updateTimeText();
        }

        @Override
        public void onItemRemoved(int index) {}

        @Override
        public void onItemChanged(int index) {}

        @Override
        public void onItemMoved(int oldIndex, int newIndex) {}

        @Override
        public void onItemInserted(int index) {}
    }
}
