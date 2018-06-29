package mvasoft.timetracker.ui.extlist;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.drextended.actionhandler.listener.ActionClickListener;

import javax.inject.Inject;

import mvasoft.dialogs.AlertDialogFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.recyclerbinding.adapter.BindableListAdapter;
import mvasoft.recyclerbinding.delegate.BindableListDelegate;
import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentSessionListExBinding;
import mvasoft.timetracker.databinding.ListItemDayBinding;
import mvasoft.timetracker.databinding.ListItemSessionBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.editsession.EditSessionActivity;


public class ExSessionListFragment
        extends BindingSupportFragment<FragmentSessionListExBinding, ExSessionListViewModel>
        implements DialogResultListener {

    private static final String ARGS_DATE_START = "args_date_MIN";
    private static final String ARGS_DATE_END = "args_date_MAX";

    private static final int DLG_REQUEST_DELETE_SESSION = 1;

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
        return v;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            getViewModel().restoreState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getViewModel().saveState(outState);
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
        BindableListAdapter adapter = new BindableListAdapter(this,
                getViewModel().getListModel(),
                sessionDelegate, dayDelegate
        );

        adapter.setHasStableIds(true);

        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true);
        lm.setReverseLayout(true);

        RecyclerView recyclerView = getBinding().itemsView;
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        getViewModel().getListModel().hasSelectedItems().observe(this, (ignored) ->
                updateActionMode());
    }

    public void setDate(long dateStart, long dateEnd) {
        getViewModel().setDate(dateStart, dateEnd);
    }


    @Override
    public void onDialogResult(@NonNull DialogResultData data) {
        switch (data.requestCode) {
            case DLG_REQUEST_DELETE_SESSION: {
                getViewModel().deleteSelected();
                if (mActionMode != null)
                    mActionMode.finish();
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
        }

    }

    private void editSession(long sessionId) {
        Intent intent = new Intent(getContext(), EditSessionActivity.class);
        intent.putExtras(EditSessionActivity.makeArgs(sessionId));
        startActivity(intent);
    }

    private void actionSelect(Object model) {
        if (!(model instanceof ItemViewModel))
            return;

        ((ItemViewModel) model).setIsSelected(true);
    }

    private void updateActionMode() {
        if (getActivity() == null)
            return;

        int cnt = getViewModel().getListModel().getSelectedItemsCount();
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
        if (getViewModel().copySelectedToClipboard())
            Toast.makeText(getContext(), R.string.msg_selected_items_copied_to_clipboard,
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), R.string.msg_cannot_copy_to_clipboard,
                    Toast.LENGTH_LONG).show();
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
            getViewModel().getListModel().deselectAll();
        }

    }

    public static class ExSessionListActionType {
        public static final String CLICK = "click";
        public static final String SELECT = "select";
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
}
