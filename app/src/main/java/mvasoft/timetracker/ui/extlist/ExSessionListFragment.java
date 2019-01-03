package mvasoft.timetracker.ui.extlist;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import mvasoft.dialogs.AlertDialogFragment;
import mvasoft.dialogs.DatePickerFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.recyclerbinding.adapter.BindableListAdapter;
import mvasoft.recyclerbinding.delegate.BindableListDelegate;
import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.BuildConfig;
import mvasoft.timetracker.R;
import mvasoft.timetracker.events.SessionToggledEvent;
import mvasoft.timetracker.events.SessionsDeletedEvent;
import mvasoft.timetracker.events.SnackbarEvent;
import mvasoft.timetracker.databinding.FragmentSessionListExBinding;
import mvasoft.timetracker.databinding.ListItemDayBinding;
import mvasoft.timetracker.databinding.ListItemSessionBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.common.FabProvider;
import mvasoft.timetracker.ui.common.NavigationController;
import mvasoft.timetracker.ui.editsession.EditSessionFragment;
import mvasoft.timetracker.utils.DateTimeHelper;


public class ExSessionListFragment
        extends BindingSupportFragment<FragmentSessionListExBinding, ExSessionListViewModel>
        implements DialogResultListener {

    private static final String ARGS_DATE_START = "args_date_MIN";
    private static final String ARGS_DATE_END = "args_date_MAX";

    private static final int DLG_REQUEST_DELETE_SESSION = 1;
    private static final int DLG_REQUEST_DATE = 2;
    private static final String DATE_PICKER_TAG = "ExSessionListFragmentSelectDateDlg";

    private ActionMode.Callback mActionModeCallbacks;
    private ActionMode mActionMode;


    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    private FabProvider mFabProvider;
    private View.OnClickListener mFabListener;

    public static ExSessionListFragment newInstance(long minDate, long maxDate) {
        ExSessionListFragment fragment = new ExSessionListFragment();
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
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        initAdapter();
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
        if (savedInstanceState != null)
            getViewModel().restoreState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getViewModel().saveState(outState);
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
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        new DatePickerFragment.Builder(DLG_REQUEST_DATE)
                .withUnixTime(System.currentTimeMillis() / 1000)
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
                break;
            }
            case DLG_REQUEST_DATE: {
                onDateSelected((DatePickerFragment.DatePickerDialogResultData) data);
                break;
            }
        }
    }

    public void onDateSelected(DatePickerFragment.DatePickerDialogResultData data) {
        long unixTime = DateTimeHelper.getUnixTime(data.year, data.month, data.dayOfMonth);
        setDate(unixTime, unixTime);
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
        navigationController.editSession(sessionId);
//        if (getActivity() instanceof NavigationController)
//            ((NavigationController) getActivity()).showFragment(() -> EditSessionFragment.newInstance(sessionId));
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
