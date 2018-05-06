package mvasoft.timetracker.ui.extlist.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.drextended.actionhandler.listener.ActionClickListener;
import com.drextended.rvdatabinding.ListConfig;

import java.util.List;

import javax.inject.Inject;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentSessionListExBinding;
import mvasoft.timetracker.databinding.recyclerview.BaseItemModel;
import mvasoft.timetracker.databinding.recyclerview.LiveBindableAdapter;
import mvasoft.timetracker.databinding.recyclerview.ModelItemIdDelegate;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.extlist.modelview.ExSessionListViewModel;
import mvasoft.timetracker.ui.extlist.modelview.SessionItemViewModel;

import static mvasoft.timetracker.common.Const.LOG_TAG;


public class ExSessionListFragment extends BindingSupportFragment<FragmentSessionListExBinding, ExSessionListViewModel> {

    private static final String ARGS_DATE = "args_date";

    @SuppressWarnings("FieldCanBeLocal")
    private LiveBindableAdapter<List<BaseItemModel>> mAdapter;
    private ActionMode.Callback mActionModeCallbacks;
    private ActionMode mActionMode;
    private long mDate;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    public static Fragment newInstance(long date) {
        Fragment fragment = new ExSessionListFragment();
        Bundle args = new Bundle();
        args.putLong(ARGS_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            mDate = getArguments().getLong(ARGS_DATE);

        // TODO: safe group type in savedInstanceState
        mActionModeCallbacks = new ActionModeCallbacks();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // TODO: move init into new onAfterCreateViewModel() method
        initAdapter();
        updateActionMode();
        return v;
    }

    protected ExSessionListViewModel onCreateViewModel() {
        ExSessionListViewModel vm = ViewModelProviders.of(this, viewModelFactory)
                .get(ExSessionListViewModel.class);
        vm.setDate(mDate);
        return vm;
    }

    protected @LayoutRes int getLayoutId() {
        return R.layout.fragment_session_list_ex;
    }

    protected @IdRes int getModelVariableId() {
        return BR.view_model;
    }

    private void initAdapter() {
        //noinspection unchecked
        mAdapter = new LiveBindableAdapter<>(
                new ModelItemIdDelegate<>(new ExSessionListActionHandler(),
                        SessionItemViewModel.class,
                        R.layout.ex_session_list_item, BR.SessionGroup, BR.actionHandler)
        );
        mAdapter.setHasStableIds(true);
        mAdapter.setData(this, getViewModel().getListModel());
        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL))
                .build(getContext());
        listConfig.applyConfig(getContext(), getBinding().itemsView);
    }

    public void setDate(long date) {
        mDate = date;
        getViewModel().setDate(date);
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

            Log.d(LOG_TAG, "Action fired: " + actionType);
        }
    }

    private void actionClick(Object model) {
        if (!(model instanceof SessionItemViewModel))
            return;

        SessionItemViewModel groupModel = (SessionItemViewModel) model;
        if ((mActionMode == null) &&
                (getActivity() instanceof ISessionListCallbacks))
            ((ISessionListCallbacks) getActivity()).editSession(groupModel.getId());
        else if (mActionMode != null) {
            groupModel.setIsSelected(!groupModel.getIsSelected());
            updateActionMode();
        }

    }

    private void actionSelect(Object model) {
        if (getActivity() == null)
            return;

        if (!(model instanceof SessionItemViewModel))
            return;

        SessionItemViewModel groupModel = (SessionItemViewModel) model;

        groupModel.setIsSelected(true);
        updateActionMode();
    }

    private void updateActionMode() {
        if (getActivity() == null)
            return;

        int cnt = getViewModel().getSelectedItemsCount();
        if (cnt > 0) {
            if (mActionMode == null)
                ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallbacks);
            if (mActionMode != null)
                mActionMode.setTitle(String.format(getString(R.string.title_session_selected), cnt));
        }
        else if (mActionMode != null)
            mActionMode.finish();
    }



    private void deleteSelected() {
        showDialog(R.string.msg_selected_session_will_removed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LiveData<Integer> result = getViewModel().deleteSelected();
                result.observe(ExSessionListFragment.this, new Observer<Integer>() {
                    @Override
                    public void onChanged(@Nullable Integer cnt) {
                        Toast.makeText(getContext(), cnt + " session was removed.", Toast.LENGTH_LONG).show();
                    }
                });
                if (mActionMode != null)
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

    interface ISessionListCallbacks {
        void editSession(long sessionId);
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
            getViewModel().deselectAll();
        }

    }

    public static class ExSessionListActionType {
        public static final String CLICK = "click";
        public static final String SELECT = "select";
    }
}
