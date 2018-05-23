package mvasoft.timetracker.ui.extlist.view;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
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

import com.drextended.actionhandler.listener.ActionClickListener;

import javax.inject.Inject;

import mvasoft.recyclerbinding.adapter.BindableListAdapter;
import mvasoft.recyclerbinding.delegate.BindableListDelegate;
import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentSessionListExBinding;
import mvasoft.timetracker.databinding.ListItemDayBinding;
import mvasoft.timetracker.databinding.ListItemSessionBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.editsession.view.EditSessionActivity;
import mvasoft.timetracker.ui.extlist.modelview.DayItemViewModel;
import mvasoft.timetracker.ui.extlist.modelview.ExSessionListViewModel;
import mvasoft.timetracker.ui.extlist.modelview.SessionItemViewModel;


public class ExSessionListFragment extends BindingSupportFragment<FragmentSessionListExBinding, ExSessionListViewModel> {

    private static final String ARGS_DATE_START = "args_date_MIN";
    private static final String ARGS_DATE_END = "args_date_MAX";

    @SuppressWarnings("FieldCanBeLocal")
    private BindableListAdapter mAdapter;
    private ActionMode.Callback mActionModeCallbacks;
    private ActionMode mActionMode;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    public static Fragment newInstance(long minDate, long maxDate) {
        Fragment fragment = new ExSessionListFragment();
        Bundle args = new Bundle();
        args.putLong(ARGS_DATE_START, minDate);
        args.putLong(ARGS_DATE_END, maxDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: save viewModel state
        if (savedInstanceState == null && getArguments() != null) {
            long now = System.currentTimeMillis() / 1000;
            getViewModel().setDate(getArguments().getLong(ARGS_DATE_START, now),
                    getArguments().getLong(ARGS_DATE_END, now));
        }

        mActionModeCallbacks = new ActionModeCallbacks();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        initAdapter();
        updateActionMode();
        return v;
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

    private void initAdapter() {
        if (getContext() == null)
            return;

        BindableListDelegate<ListItemSessionBinding> sessionDelegate =
                new BindableListDelegate<>(this, R.layout.list_item_session,
                        BR.list_model, BR.view_model, SessionItemViewModel.class);
        ExSessionListActionHandler actionHandler = new ExSessionListActionHandler();
        sessionDelegate.setActionHandler(BR.actionHandler, actionHandler);

        BindableListDelegate<ListItemDayBinding> dayDelegate =
                new BindableListDelegate<>(this, R.layout.list_item_day,
                        BR.list_model, BR.view_model, DayItemViewModel.class);
        dayDelegate.setActionHandler(BR.actionHandler, actionHandler);
        //noinspection unchecked
        mAdapter = new BindableListAdapter(this,
                getViewModel().getListModel(),
                sessionDelegate, dayDelegate
        );
        mAdapter.setHasStableIds(true);

        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true);
        lm.setReverseLayout(true);

        RecyclerView recyclerView = getBinding().itemsView;
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(mAdapter);
    }

    public void setDate(long dateStart, long dateEnd) {
        getViewModel().setDate(dateStart, dateEnd);
    }

    private class ExSessionListActionHandler implements ActionClickListener {

        @Override
        public void onActionClick(View view, String actionType, Object model) {
            switch (actionType) {
                case ExSessionListActionType.CLICK:
                    actionClick(model);
                    break;

                case ExSessionListActionType.SELECT:
                    actionSelect(model);
                    break;
            }
        }
    }

    private void actionClick(Object model) {
        if (!(model instanceof ItemViewModel))
            return;

        ItemViewModel groupModel = (ItemViewModel) model;
        if ((mActionMode == null) && (groupModel instanceof SessionItemViewModel))
            editSession(groupModel.getId());
        else if (mActionMode != null) {
            ((ItemViewModel) model).toggleSelection();
            updateActionMode();
        }

    }

    private void editSession(long sessionId) {
        Intent intent = new Intent(getContext(), EditSessionActivity.class);
        intent.putExtras(EditSessionActivity.makeArgs(sessionId));
        startActivity(intent);
    }

    private void actionSelect(Object model) {
        if (getActivity() == null)
            return;

        if (!(model instanceof ItemViewModel))
            return;

        getViewModel().getListModel().startSelection();
        ((ItemViewModel) model).setIsSelected(true);
        updateActionMode();
    }

    private void updateActionMode() {
        if (getActivity() == null)
            return;

        int cnt = getViewModel().getListModel().getSelectedItemsCount();
        if (getViewModel().getListModel().isPendingSelection()) {
            if (mActionMode == null)
                ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallbacks);
            if (mActionMode != null)
                mActionMode.setTitle(String.format(getString(R.string.title_session_selected), cnt));
        } else if (mActionMode != null)
            mActionMode.finish();
    }

    private void deleteSelected() {
        // TODO: make undo on delete
        showDialog(R.string.msg_selected_session_will_removed, (dialog, which) -> {
            getViewModel().deleteSelected();
            if (mActionMode != null)
                mActionMode.finish();
        });
    }

    private void showDialog(@StringRes int msgId, DialogInterface.OnClickListener onOkListener) {
        if (getActivity() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msgId);
        builder.setPositiveButton(android.R.string.ok, onOkListener);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
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
                    getViewModel().copySelectedToClipboard();
                    break;
                default:
                    res = false;
            }
            return res;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            getViewModel().getListModel().endSelection();
        }

    }

    public static class ExSessionListActionType {
        public static final String CLICK = "click";
        public static final String SELECT = "select";
    }
}
