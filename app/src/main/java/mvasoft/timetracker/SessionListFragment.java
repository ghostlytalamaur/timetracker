package mvasoft.timetracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.ItemSelectionSupport;

import java.util.Timer;
import java.util.TimerTask;


import static android.content.Context.MODE_PRIVATE;
import static mvasoft.timetracker.Consts.LOADER_ID_GROUPS_CURRENT;
import static mvasoft.timetracker.Consts.LOADER_ID_GROUPS_TODAY;
import static mvasoft.timetracker.Consts.LOADER_ID_GROUPS_WEEK;


public class SessionListFragment extends Fragment {

    private static final String LOGT = "mvasoft.timetracker";
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
    private GroupsChangesListener mGroupListener;
    private ISessionListCallbacks mCallbacks;

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

        mGroupListener = new GroupsChangesListener();

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

        mLoaderCallbacks = new GroupsLoaderCallbacks(getContext(), mGroupInfoProvider,
                mCurrentGroups, mTodayGroup, mWeekGroup);

        LoaderManager lm = getActivity().getSupportLoaderManager();
        lm.restartLoader(LOADER_ID_GROUPS_WEEK, null, mLoaderCallbacks);
        lm.restartLoader(LOADER_ID_GROUPS_CURRENT, null, mLoaderCallbacks);
        lm.restartLoader(LOADER_ID_GROUPS_TODAY,null, mLoaderCallbacks);

        setHasOptionsMenu(true);

        if (getActivity() instanceof ISessionListCallbacks)
            mCallbacks = (ISessionListCallbacks) getActivity();
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
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mUpdateViewRunnable);
            }
        }, 1000, 1000);

        mCurrentGroups.addChangesListener(mGroupListener);
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUpdateTimer.cancel();
        mUpdateTimer.purge();
        mUpdateTimer = null;

        mCurrentGroups.removeChangesListener(mGroupListener);
    }

    @Override
    public void onDestroy() {
        mTodayGroup.swapCursor(null);
        mWeekGroup.swapCursor(null);
        mCurrentGroups.swapCursor(null);
        super.onDestroy();
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
        spGroups.setSelection(mGroupInfoProvider.getCurrentGroupType().ordinal());
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_fill_fake_sessions: {
                fillFakeSession();
                return true;
            }
            case R.id.action_import_db: {
                importDB();
                return true;
            }
            case R.id.action_export_db: {
                exportDB();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportDB() {
        if (BackupAssistant.backupDb(getActivity()))
            Toast.makeText(getContext(), R.string.backup_succeeded, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), R.string.backup_failed, Toast.LENGTH_SHORT).show();
    }

    private void importDB() {
        showDialog(R.string.msg_all_data_will_removed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (BackupAssistant.restoreDb(getActivity()))
                    Toast.makeText(getContext(), R.string.import_succeeded, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), R.string.import_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillFakeSession() {
        showDialog(R.string.msg_all_data_will_removed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FakeSessionFiller.fill(getContext());
            }
        });
    }

    private void deleteSelected() {
        showDialog(R.string.msg_selected_session_will_removed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((mSessionHelper.deleteGroups(mGroupInfoProvider.getCurrentGroupsUri(),
                        mSelectionSupport.getCheckedItemIds())) && (mActionMode != null))
                    mActionMode.finish();
            }
        });
    }

    private void showDialog(@StringRes int msgId, DialogInterface.OnClickListener onOkListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msgId);
        builder.setPositiveButton(R.string.button_ok, onOkListener);
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.show();
    }

    private void copySelectedToClipboard() {

        final ClipboardManager clpbrd = (ClipboardManager)
                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clpbrd == null)
            return;

        final PeriodFormatter periodFormatter = new PeriodFormatterBuilder().
                printZeroAlways().
                minimumPrintedDigits(2).
                appendHours().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendMinutes().
                toFormatter();

        final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().
                appendDayOfWeekText().
                appendLiteral(", ").
                appendDayOfMonth(2).
                appendLiteral(" ").
                appendMonthOfYearText().
                toFormatter();


        long ids[] = mSelectionSupport.getCheckedItemIds();
        String text = "";
        for (long id : ids) {
            GroupsList.SessionGroup group = mCurrentGroups.getByID(id);
            switch (mGroupInfoProvider.getCurrentGroupType()) {
                case gt_Day: {
                    text = text + String.format("%s: %s\n",
                            dateFormatter.print(new DateTime(group.getStart() * 1000L)),
                            periodFormatter.print(new Period(group.getDuration() * 1000L)));
                    break;
                }
                default: {
                    text = text + String.format("%s - %s: %s\n",
                            dateFormatter.print(new DateTime(group.getStart() * 1000L)),
                            dateFormatter.print(new DateTime(group.getEnd() * 1000L)),
                            periodFormatter.print(new Period(group.getDuration() * 1000L)));
                    break;
                }
            }
        }

        clpbrd.setPrimaryClip(ClipData.newPlainText("Sessions", text));
    }

    private void setCurrentGroupType(GroupType type) {
        if (type == mGroupInfoProvider.getCurrentGroupType())
            return;

        mGroupInfoProvider.setCurrentGroupType(type);
        getActivity().getPreferences(MODE_PRIVATE).edit().
                putInt(PreferencesConst.GROUP_TYPE, type.ordinal()).apply();
        getActivity().getSupportLoaderManager().restartLoader(
                LOADER_ID_GROUPS_CURRENT, null, mLoaderCallbacks);
    }

    private void updateUI() {
        if (!isAdded())
            return;
        Log.d(LOGT, "SessionListFragment.updateUI()");
        boolean hasOpened = mCurrentGroups.hasOpenedSessions();
        Log.d(LOGT, "hasOpened = " + hasOpened);
        if (hasOpened)
            mFab.setImageResource(R.drawable.animated_minus);
        else
            mFab.setImageResource(R.drawable.animated_plus);
        if (mFab.getDrawable() instanceof Animatable)
            ((Animatable) mFab.getDrawable()).start();
        updateTimeText();
    }

    private void updateTimeText() {
        if (!isAdded())
            return;

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

    private void updateActionMode() {
        if (mActionMode == null)
            return;

        int cnt = mSelectionSupport.getCheckedItemCount();
        if (cnt > 0)
            mActionMode.setTitle(String.format(getString(R.string.title_session_selected), cnt));
        else
            mActionMode.finish();
    }


    interface ISessionListCallbacks {
        void editSession(long sessionId);
    }

    private class SessionClickListener implements ItemClickSupport.OnItemClickListener,
            ItemClickSupport.OnItemLongClickListener {
        @Override
        public void onItemClick(RecyclerView parent, View view, int position, long id) {
            if ((mActionMode == null) && (mCallbacks != null) &&
                    (mGroupInfoProvider.getCurrentGroupType() == GroupType.gt_None)) {
                mCallbacks.editSession(id);
            }
            updateActionMode();
        }

        @Override
        public boolean onItemLongClick(RecyclerView parent, View view, int position, long id) {
            if (mActionMode == null)
                ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallbacks);

            mSelectionSupport.setItemChecked(position, true);
            updateActionMode();
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
            boolean res = true;
            switch (item.getItemId()) {
                case R.id.action_delete_selected:
                    deleteSelected();
                    break;
                case R.id.action_copy_selected:
                    copySelectedToClipboard();
                    break;
                default:
                    res = false;
            }
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
                    Snackbar.make(mFab, R.string.session_started, Snackbar.LENGTH_LONG).show();
                    break;

                case tgs_Stopped:
                    Snackbar.make(mFab, R.string.session_stopped, Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private class GroupsChangesListener implements GroupsList.IGroupsChangesListener {
        @Override
        public void onDataChanged() {
            updateUI();
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
