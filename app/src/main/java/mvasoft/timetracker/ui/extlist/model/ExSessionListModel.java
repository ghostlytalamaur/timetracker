package mvasoft.timetracker.ui.extlist.model;

import android.arch.lifecycle.LiveData;

import dagger.Lazy;
import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.data.DataRepository;

public class ExSessionListModel {

    private GroupType mGroupType;
    private LiveData<GroupsList> mGroups;
    private Lazy<DataRepository> mRepository;

    public ExSessionListModel(Lazy<DataRepository> repository) {
        mRepository = repository;
        mGroupType = GroupType.gt_None;
    }

    public void setGroupType(GroupType groupType) {
        if (mGroupType == groupType)
            return;

        mGroupType = groupType;

    }

    public LiveData<GroupsList> getGroups() {
        if (mGroups == null)
            mGroups = mRepository.get().getGroups(mGroupType);
        return mGroups;
    }

    public GroupType getGroupType() {
        return mGroupType;
    }
}
