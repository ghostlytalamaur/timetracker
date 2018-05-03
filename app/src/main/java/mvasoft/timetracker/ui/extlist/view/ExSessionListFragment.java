package mvasoft.timetracker.ui.extlist.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentSessionListExBinding;
import mvasoft.timetracker.databinding.recyclerview.BaseItemModel;
import mvasoft.timetracker.databinding.recyclerview.LiveBindableAdapter;
import mvasoft.timetracker.databinding.recyclerview.ModelItemIdDelegate;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.extlist.modelview.ExSessionListViewModel;
import mvasoft.timetracker.ui.extlist.modelview.SessionGroupViewModel;

import static mvasoft.timetracker.deprecated.Consts.LOG_TAG;

public class ExSessionListFragment extends BindingSupportFragment<FragmentSessionListExBinding, ExSessionListViewModel> {

    private static final String ARGS_GROUP_TYPE = "ARGS_GROUP_TYPE";
    @SuppressWarnings("FieldCanBeLocal")
    private LiveBindableAdapter<List<BaseItemModel>> mAdapter;
    private GroupType mGroupType;
    private ActionMode.Callback mActionModeCallbacks;
    private ActionMode mActionMode;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    public static Fragment newInstance(GroupType groupType) {
        Fragment fragment = new ExSessionListFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_GROUP_TYPE, groupType.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: safe group type in savedInstanceState
        Bundle args = getArguments();
        mGroupType = GroupType.gt_None;
        if (args != null)
            mGroupType = GroupType.values()[args.getInt(ARGS_GROUP_TYPE, GroupType.gt_None.ordinal())];
        mActionModeCallbacks = new ActionModeCallbacks();

        Log.d(LOG_TAG, "ExSessionListFragment.onCreate() with " + mGroupType.toString());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        Log.d(LOG_TAG, "ExSessionListFragment.onCreateView() with " + mGroupType.toString());
        // TODO: move init into new onAfterCreateViewModel() method
        initAdapter();
        updateActionMode();
        return v;
    }

    protected ExSessionListViewModel onCreateViewModel() {
        ExSessionListViewModel vm = ViewModelProviders.of(this, viewModelFactory)
                .get(ExSessionListViewModel.class);

        vm.setGroupType(mGroupType);
        return vm;
    }

    protected @LayoutRes int getLayoutId() {
        return R.layout.fragment_session_list_ex;
    }

    protected @IdRes int getModelVariableId() {
        return BR.view_model;
    }

    private void initAdapter() {
        Log.d(LOG_TAG, "ExSessionListFragment.initAdapter() with " + mGroupType.toString());

        //noinspection unchecked
        mAdapter = new LiveBindableAdapter<>(
                new ModelItemIdDelegate<BaseItemModel>(new ExSessionListActionHandler(),
                        SessionGroupViewModel.class,
                        R.layout.ex_session_list_item, BR.SessionGroup, BR.actionHandler)
        );
        mAdapter.setHasStableIds(true);
        mAdapter.setData(this, getViewModel().getListModel());
        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.LayoutManagerProvider() {
                    @Override
                    public RecyclerView.LayoutManager get(Context context) {
                        LinearLayoutManager lm = new LinearLayoutManager(context);
                        lm.setStackFromEnd(false);
                        return lm;
                    }
                })
                .build(getContext());
        listConfig.applyConfig(getContext(), getBinding().itemsView);
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
        if (!(model instanceof SessionGroupViewModel))
            return;

        SessionGroupViewModel groupModel = (SessionGroupViewModel) model;
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

        if (!(model instanceof SessionGroupViewModel))
            return;

        SessionGroupViewModel groupModel = (SessionGroupViewModel) model;

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
            mActionMode.setTag(mGroupType);
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
