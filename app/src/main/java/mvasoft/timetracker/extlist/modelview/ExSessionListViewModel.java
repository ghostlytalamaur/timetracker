package mvasoft.timetracker.extlist.modelview;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.extlist.model.BaseItemModel;
import mvasoft.timetracker.extlist.model.ExSessionListModel;
import mvasoft.timetracker.ui.DateTimeFormatters;
import mvasoft.timetracker.ui.base.BaseViewModel;

import static mvasoft.timetracker.Consts.LOG_TAG;

public class ExSessionListViewModel extends BaseViewModel {


    private ExSessionListModel mModel;
    private final DateTimeFormatters mFormatter;
    private MutableLiveData<List<BaseItemModel>> mListModel;

    public ExSessionListViewModel(@NonNull Application application) {
        super(application);
        mFormatter = new DateTimeFormatters();
        Log.d(LOG_TAG, "creating ExSessionListViewModel");
    }

    private void updateListData() {
        if (mListModel != null)
            mListModel.setValue(buildListData());
    }

    public LiveData<List<BaseItemModel>> getListModel() {
        if (mListModel == null) {
            mListModel = new MutableLiveData<>();
            updateListData();
        }
        return mListModel;
    }

    private List<BaseItemModel> buildListData() {
        ArrayList<BaseItemModel> items = new ArrayList<>();
        if (mModel != null) {
            GroupsList groups = mModel.getGroups();
            for (int i = 0; i < groups.count(); i++) {
                items.add(new SessionGroupViewModel(mFormatter, groups.get(i)));
            }
        }
        return items;
    }

    public void setGroupType(GroupType groupType) {
        if ((mModel != null) && (mModel.getGroupType() == groupType))
            return;

        mModel = new ExSessionListModel(groupType);
        mModel.getGroups().addChangesListener(new GroupsList.IGroupsChangesListener() {
            @Override
            public void onDataChanged() {
                updateListData();
            }
        });
    }

    public ExSessionListModel getModel() {
        return mModel;
    }

    @Override
    protected void onCleared() {
        Log.d(LOG_TAG, "ExSessionListViewModel.onCleared() with " + mModel.getGroupType().toString());
        super.onCleared();
    }

    public static class ExSessionListViewModelFactory extends ViewModelProvider.NewInstanceFactory {


        private Application mApplication;
        private GroupType mGroupType;

        public ExSessionListViewModelFactory(Application application, GroupType groupType) {
            mApplication = application;
            mGroupType = groupType;
        }

        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ExSessionListViewModel.class)) {
                ExSessionListViewModel vm = new ExSessionListViewModel(mApplication);
                vm.setGroupType(mGroupType);
                return (T) vm;
            }
            return super.create(modelClass);
        }
    }

}
