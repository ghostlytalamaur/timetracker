package mvasoft.timetracker.deprecated;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.ItemSelectionSupport;

import java.util.Objects;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentSessionListBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.utils.DateTimeFormatters;

import static android.content.Context.MODE_PRIVATE;
import static mvasoft.timetracker.deprecated.Const.LOADER_ID_GROUPS_CURRENT;
import static mvasoft.timetracker.deprecated.Const.LOADER_ID_GROUPS_TODAY;
import static mvasoft.timetracker.deprecated.Const.LOADER_ID_GROUPS_WEEK;


public class SessionListFragment extends BindingSupportFragment<FragmentSessionListBinding, SessionListViewModel> {

    private SessionsAdapter mAdapter;

    private DateTimeFormatters mFormatter;
    private GroupsList mCurrentGroups;
    private GroupsList mTodayGroup;
    private GroupsList mWeekGroup;

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

        mFormatter = new DateTimeFormatters();

        mGroupInfoProvider = new GroupInfoProvider();
        mGroupInfoProvider.setCurrentGroupType(
                GroupType.values()[(getActivity() == null) ? 0 :  getActivity().getPreferences(MODE_PRIVATE).
                getInt(PreferencesConst.GROUP_TYPE, GroupType.gt_None.ordinal())]
        );

        mSessionHelper = new SessionHelper(Objects.requireNonNull(getContext()));

        mTodayGroup = new GroupsList();
        mWeekGroup = new GroupsList();
        mCurrentGroups = new GroupsList();

        mGroupListener = new GroupsChangesListener();

        mAdapter = new SessionsAdapter();
        mAdapter.setList(mCurrentGroups);

        mActionModeCallbacks = new ActionModeCallbacks();

        mLoaderCallbacks = new GroupsLoaderCallbacks(getContext(), mGroupInfoProvider,
                mCurrentGroups, mTodayGroup, mWeekGroup);

        if (getActivity() != null) {
            LoaderManager lm = getActivity().getSupportLoaderManager();
            lm.restartLoader(LOADER_ID_GROUPS_WEEK, null, mLoaderCallbacks);
            lm.restartLoader(LOADER_ID_GROUPS_CURRENT, null, mLoaderCallbacks);
            lm.restartLoader(LOADER_ID_GROUPS_TODAY, null, mLoaderCallbacks);
        }

        setHasOptionsMenu(true);

        if (getActivity() instanceof ISessionListCallbacks)
            mCallbacks = (ISessionListCallbacks) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View fragmentView = super.onCreateView(inflater, container, savedInstanceState);

        mFab = getBinding().fab;
        mFab.setOnClickListener(new FabClickListener());

        final RecyclerView itemsView = getBinding().itemsView;
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
    protected SessionListViewModel onCreateViewModel() {
        SessionListViewModel vm = ViewModelProviders.of(this).get(SessionListViewModel.class);
        vm.setup(mTodayGroup, mWeekGroup);
        return vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_session_list;
    }

    @Override
    protected int getModelVariableId() {
        return BR.session_list_view_model;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentGroups.addChangesListener(mGroupListener);
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();

        mCurrentGroups.removeChangesListener(mGroupListener);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_group_type);
        Spinner spGroups = (Spinner) item.getActionView();

        final ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()),
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
        if (getActivity() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msgId);
        builder.setPositiveButton(R.string.button_ok, onOkListener);
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.show();
    }

    private void copySelectedToClipboard() {
        if (getActivity() == null)
            return;

        final ClipboardManager clipboard = (ClipboardManager)
                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return;

        long ids[] = mSelectionSupport.getCheckedItemIds();
        StringBuilder text = new StringBuilder();
        for (long id : ids) {
            GroupsList.SessionGroup group = mCurrentGroups.getByID(id);
            switch (mGroupInfoProvider.getCurrentGroupType()) {
                case gt_Day: {
                    text.append(String.format("%s: %s\n",
                            mFormatter.formatDate(group.getStart()),
                            mFormatter.formatDate(group.getDuration())));
                    break;
                }
                default: {
                    text.append(String.format("%s - %s: %s\n",
                            mFormatter.formatDate(group.getStart()),
                            mFormatter.formatDate(group.getEnd()),
                            mFormatter.formatPeriod(group.getDuration())));
                    break;
                }
            }
        }

        clipboard.setPrimaryClip(ClipData.newPlainText("Sessions", text.toString()));
    }

    private void setCurrentGroupType(GroupType type) {
        if ((getActivity() == null) || (type == mGroupInfoProvider.getCurrentGroupType()))
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

        mAdapter.updateNotClosedView();
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
            if (getActivity() == null)
                return false;

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
    }

}
