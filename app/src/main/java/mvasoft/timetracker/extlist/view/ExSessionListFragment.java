package mvasoft.timetracker.extlist.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.drextended.actionhandler.listener.ActionClickListener;
import com.drextended.rvdatabinding.ListConfig;

import java.util.List;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.GroupInfoProvider;
import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.R;
import mvasoft.timetracker.data.DatabaseDescription;
import mvasoft.timetracker.databinding.FragmentSessionListExBinding;
import mvasoft.timetracker.databinding.recyclerview.LiveBindableAdapter;
import mvasoft.timetracker.databinding.recyclerview.ModelItemIdDelegate;
import mvasoft.timetracker.extlist.model.BaseItemModel;
import mvasoft.timetracker.extlist.modelview.ExSessionListViewModel;
import mvasoft.timetracker.extlist.modelview.SessionGroupViewModel;
import mvasoft.timetracker.ui.base.BindingSupportFragment;

import static mvasoft.timetracker.Consts.LOG_TAG;

public class ExSessionListFragment extends BindingSupportFragment<FragmentSessionListExBinding, ExSessionListViewModel> {

    private static final String ARGS_GROUP_TYPE = "ARGS_GROUP_TYPE";
    private LiveBindableAdapter<List<BaseItemModel>, LiveData<List<BaseItemModel>>> mAdapter;
    private GroupType mGroupType;

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

        Log.d(LOG_TAG, "ExSessionListFragment.onCreate() with " + mGroupType.toString());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // TODO: move init into new onAfterCreateViewModel() method
        initAdapter();
        initLoader();
        return v;
    }


    protected ExSessionListViewModel onCreateViewModel() {
        ExSessionListViewModel.ExSessionListViewModelFactory factory =
                new ExSessionListViewModel.ExSessionListViewModelFactory(
                        getActivity().getApplication(), mGroupType);

        return ViewModelProviders.of(this, factory).get(ExSessionListViewModel.class);
    }

    protected @LayoutRes int getLayoutId() {
        return R.layout.fragment_session_list_ex;
    }

    protected @IdRes int getModelVariableId() {
        return BR.view_model;
    }

    private void initAdapter() {
        Log.d(LOG_TAG, "ExSessionListFragment.initAdapter() with " + mGroupType.toString());
        Log.d(LOG_TAG, "Adapter is null = " + String.valueOf(mAdapter == null));

        mAdapter = new LiveBindableAdapter<>(
                new ModelItemIdDelegate<BaseItemModel>(new ExSessionListActionHandler(),
                        SessionGroupViewModel.class,
                        R.layout.ex_session_list_item, BR.SessionGroup, BR.actionHandler)
        );
        mAdapter.setHasStableIds(true);
        mAdapter.setData(this, getViewModel().getListModel());
        ListConfig listConfig = new ListConfig.Builder(mAdapter)
                .setDefaultDividerEnabled(true)
                .build(getContext());
        listConfig.applyConfig(getContext(), getBinding().itemsView);
    }

    private void initLoader() {
        if (getActivity() == null)
            return;

        LoaderManager lm = getActivity().getSupportLoaderManager();
        lm.initLoader(1000 + mGroupType.ordinal(), null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                GroupInfoProvider info = new GroupInfoProvider();
                info.setCurrentGroupType(mGroupType);
                return new CursorLoader(getActivity(),
                        info.getCurrentGroupsUri(),
                        null, null, null,
                        DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                if (getViewModel() == null)
                    return;

                getViewModel().getModel().getGroups().swapCursor(data);
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {
                if (getViewModel() == null)
                    return;

                getViewModel().getModel().getGroups().swapCursor(null);
            }
        });
    }

    private class ExSessionListActionHandler implements ActionClickListener {

        @Override
        public void onActionClick(View view, String actionType, Object model) {
            switch (actionType) {
                case ExSessionListActionType.EDIT:
                    if ((getActivity() instanceof ISessionListCallbacks) &&
                            (model instanceof SessionGroupViewModel))
                        ((ISessionListCallbacks) getActivity()).
                                editSession(((SessionGroupViewModel) model).getId());
            }

            Log.d(LOG_TAG, "Action fired: " + actionType);
        }
    }

    interface ISessionListCallbacks {
        void editSession(long sessionId);
    }
}
