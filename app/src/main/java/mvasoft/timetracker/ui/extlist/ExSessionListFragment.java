package mvasoft.timetracker.ui.extlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import mvasoft.dialogs.AlertDialogFragment;
import mvasoft.dialogs.DatePickerFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.recyclerbinding.DiffUtilItemViewModelCallback;
import mvasoft.recyclerbinding.ItemViewModelDetailsLookup;
import mvasoft.recyclerbinding.ItemViewModelKeyProvider;
import mvasoft.recyclerbinding.ItemViewModel;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.BuildConfig;
import mvasoft.timetracker.R;
import mvasoft.timetracker.core.Injectable;
import mvasoft.timetracker.databinding.FragmentSessionListExBinding;
import mvasoft.timetracker.databinding.ListItemSessionsGroupsBinding;
import mvasoft.timetracker.events.SessionToggledEvent;
import mvasoft.timetracker.events.SessionsDeletedEvent;
import mvasoft.timetracker.events.SnackbarEvent;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.common.FabProvider;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.SessionsGroup;
import mvasoft.utils.CollectionsUtils;

import static mvasoft.dialogs.DatePickerFragment.SELECTION_MODE_RANGE;


public class ExSessionListFragment
        extends BindingSupportFragment<FragmentSessionListExBinding, ExSessionListViewModel>
        implements DialogResultListener, Injectable {

    private static final int DLG_REQUEST_DELETE_SESSION = 1;
    private static final int DLG_REQUEST_DATE = 2;
    private static final String DATE_PICKER_TAG = "ExSessionListFragmentSelectDateDlg";

    private ActionMode.Callback mActionModeCallbacks;
    private ActionMode mActionMode;


    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private FabProvider mFabProvider;
    private View.OnClickListener mFabListener;
    private SelectionTracker<Long> mSelectionTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionModeCallbacks = new ActionModeCallbacks();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        initAdapter(savedInstanceState);
        updateFab(false);
        getViewModel().getOpenedSessionId().observe(this, b -> updateFab(b != null && b));
        return v;
    }

    private void updateFab(boolean hasOpenedSession) {
        if (mFabProvider == null)
            return;

        if (hasOpenedSession)
            mFabProvider.setImageResource(R.drawable.animated_minus);
        else
            mFabProvider.setImageResource(R.drawable.animated_plus);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectionTracker.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sessions, menu);
        if (!BuildConfig.DEBUG)
            menu.removeItem(R.id.action_fill_fake_session);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_date:
                selectDate();
                break;
            case R.id.action_fill_fake_session:
                getViewModel().fillFakeSessions();
                break;
            case R.id.action_group_by:
                selectGroupType();
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void selectGroupType() {
        new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(R.string.msg_group_by)
                .setSingleChoiceItems(R.array.group_type, getViewModel().getGroupType().ordinal(), (dialog, which) -> {
                    getViewModel().setGroupType(SessionsGroup.GroupType.values()[which]);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() instanceof FabProvider) {
            mFabProvider = (FabProvider) getActivity();
            mFabProvider.setVisibility(View.VISIBLE);
            if (mFabListener == null)
                mFabListener = (v) -> getViewModel().toggleSession();
            mFabProvider.setClickListener(mFabListener);
        }
    }

    @Override
    public void onStop() {
        if (mFabProvider != null) {
            mFabProvider.setVisibility(View.GONE);
            mFabProvider.removeClickListener(mFabListener);
        }
        mFabProvider = null;
        super.onStop();
    }

    private void selectDate() {
        ExSessionListViewModel.DateRange range = getViewModel().getDateRange();
        new DatePickerFragment.Builder(DLG_REQUEST_DATE)
                .setStartDate(range.start)
                .setEndDate(range.end)
                .setSelectionMode(SELECTION_MODE_RANGE)
                .show(this, DATE_PICKER_TAG);
    }



    protected ExSessionListViewModel onCreateViewModel() {
        return ViewModelProviders.of(this, viewModelFactory)
                .get(ExSessionListViewModel.class);
    }

    protected @LayoutRes
    int getLayoutId() {
        return R.layout.fragment_session_list_ex;
    }

    protected @IdRes
    int getModelVariableId() {
        return BR.view_model;
    }

    private void initAdapter(Bundle savedInstanceState) {
        if (getContext() == null)
            return;

        ExSessionListActionHandler actionHandler = new ExSessionListActionHandler();
        ItemsViewModelAdapter adapter = new ItemsViewModelAdapter(this, actionHandler);
        adapter.setHasStableIds(true);
        getViewModel().getListModel().observe(this, adapter::submitList);

        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true);
        lm.setReverseLayout(true);

        RecyclerView recyclerView = getBinding().itemsView;
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        ItemViewModelKeyProvider keyProvider = new ItemViewModelKeyProvider(adapter, ItemKeyProvider.SCOPE_CACHED);
        mSelectionTracker = new SelectionTracker.Builder<>(
                "selection-id",
                recyclerView,
                keyProvider,
                new ItemViewModelDetailsLookup(recyclerView, keyProvider),
                StorageStrategy.createLongStorage()
        ).build();
        mSelectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onItemStateChanged(@NonNull Long key, boolean selected) {
                super.onItemStateChanged(key, selected);
                updateActionMode();
            }
        });
        adapter.setSelectionTracker(mSelectionTracker);
        if (savedInstanceState != null)
            mSelectionTracker.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDialogResult(@NonNull DialogResultData data) {
        switch (data.requestCode) {
            case DLG_REQUEST_DELETE_SESSION: {
                getViewModel().deleteSelected(CollectionsUtils.asSet(mSelectionTracker.getSelection()));
                if (mActionMode != null)
                    mActionMode.finish();
                break;
            }
            case DLG_REQUEST_DATE: {
                onDateSelected((DatePickerFragment.DatePickerDialogResultData) data);
                break;
            }
        }
    }

    public void onDateSelected(DatePickerFragment.DatePickerDialogResultData data) {
        if (CollectionsUtils.isEmpty(data.getDays()))
            return;

        CalendarDay minDate = data.getDays().get(0);
        CalendarDay maxDate = data.getDays().get(data.getDays().size() - 1);

        long start = DateTimeHelper.getUnixTime(minDate.getYear(), minDate.getMonth(), minDate.getDay());
        long end = DateTimeHelper.getUnixTime(maxDate.getYear(), maxDate.getMonth(), maxDate.getDay());
        getViewModel().setDate(new ExSessionListViewModel.DateRange(start, end));
    }

    private void updateActionMode() {
        if (getActivity() == null)
            return;


        int cnt = mSelectionTracker.getSelection().size(); // getViewModel().getListModel().getSelectedItemsCount();
        if (cnt > 0) {
            if (mActionMode == null)
                ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallbacks);
            if (mActionMode != null)
                mActionMode.setTitle(String.format(getString(R.string.title_session_selected), cnt));
        } else if (mActionMode != null)
            mActionMode.finish();
    }

    private void deleteSelected() {
        new AlertDialogFragment.Builder(DLG_REQUEST_DELETE_SESSION)
                .withMessage(getString(R.string.msg_selected_session_will_removed))
                .show(this, "ExSessionListFragment" + DLG_REQUEST_DELETE_SESSION);
    }

    private void copyToClipboard() {
        if (getViewModel().copyToClipboard(CollectionsUtils.asSet(mSelectionTracker.getSelection())))
            Toast.makeText(getContext(), R.string.msg_selected_items_copied_to_clipboard,
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), R.string.msg_cannot_copy_to_clipboard,
                    Toast.LENGTH_LONG).show();
    }

    @Override
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe
    public void onSessionToggled(SessionToggledEvent e) {
        switch (e.toggleResult) {
            case tgs_Started:
                EventBus.getDefault().post(new SnackbarEvent(R.string.msg_session_started, Snackbar.LENGTH_LONG));
                break;

            case tgs_Stopped:
                EventBus.getDefault().post(new SnackbarEvent(R.string.msg_session_stopped, Snackbar.LENGTH_LONG));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSessionsDeletedEvent(SessionsDeletedEvent e) {
        Toast.makeText(getContext(), e.deletedSessionsCount + " session was removed", Toast.LENGTH_LONG).show();
    }


    private class ActionModeCallbacks implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.action_mode, menu);
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
                    copyToClipboard();
                    break;
                default:
                    res = false;
            }
            return res;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mSelectionTracker.clearSelection();
        }

    }

    public class ExSessionListActionHandler {

        public void onItemClick(GroupItemViewModel item) {
            if ((mActionMode == null) && (item.sessionsCount() == 1)) {
                NavHostFragment.findNavController(ExSessionListFragment.this).navigate(
                        ExSessionListFragmentDirections.actionEditSession().setSessionId(item.getId()));
            }
            else if (mActionMode != null) {
                long id = item.getId();
                if (mSelectionTracker.isSelected(id))
                    mSelectionTracker.deselect(id);
                else
                    mSelectionTracker.select(id);
            }
        }

    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final ListItemSessionsGroupsBinding mBinding;

        static ItemViewHolder newInstance(@NonNull ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ListItemSessionsGroupsBinding binding = DataBindingUtil.inflate(inflater,
                    R.layout.list_item_sessions_groups, parent, false);
            return new ItemViewHolder(binding);
        }

        ItemViewHolder(ListItemSessionsGroupsBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bindTo(LifecycleOwner lifecycleOwner, SelectionTracker<Long> selectionTracker,
                    ExSessionListActionHandler actionHandler, ItemViewModel item) {
            mBinding.setLifecycleOwner(lifecycleOwner);
            mBinding.setIsSelected(selectionTracker != null && selectionTracker.isSelected(item.getId()));
            mBinding.setActionHandler(actionHandler);
            mBinding.setViewModel((GroupItemViewModel) item);

            mBinding.executePendingBindings();
        }
    }

    static class ItemsViewModelAdapter extends RecyclerView.Adapter<ItemViewHolder>
            implements ItemViewModelKeyProvider.ItemsProvider {

        private final AsyncListDiffer<ItemViewModel> mHelper;

        private SelectionTracker<Long> mSelectionTracker;
        private ExSessionListActionHandler mActionHandler;
        private LifecycleOwner mLifecycleOwner;

        public ItemsViewModelAdapter(LifecycleOwner lifecycleOwner, ExSessionListActionHandler actionHandler) {
            mLifecycleOwner = lifecycleOwner;
            mActionHandler = actionHandler;
            mHelper = new AsyncListDiffer<>(
                    new AdapterListUpdateCallback(this),
                    new AsyncDifferConfig.Builder<>(new DiffUtilItemViewModelCallback()).build());
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return ItemViewHolder.newInstance(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            holder.bindTo(mLifecycleOwner, mSelectionTracker, mActionHandler, getItem(position));
        }

        public ItemViewModel getItem(int pos) {
            return mHelper.getCurrentList().get(pos);
        }

        @Override
        public int getItemCount() {
            return mHelper.getCurrentList().size();
        }

        @Override
        public List<ItemViewModel> getItems() {
            return mHelper.getCurrentList();
        }

        void submitList(List<ItemViewModel> list) {
            mHelper.submitList(list);
        }

        @Override
        public long getItemId(int position) {
            return mHelper.getCurrentList().get(position).getId();
        }

        void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
            mSelectionTracker = selectionTracker;
        }
    }
}
