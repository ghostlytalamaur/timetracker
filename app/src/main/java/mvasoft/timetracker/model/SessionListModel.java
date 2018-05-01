package mvasoft.timetracker.model;

import mvasoft.timetracker.GroupInfoProvider;
import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;

public class SessionListModel {

    private GroupInfoProvider mGroupInfoProvider;
    private GroupsList mCurrentGroups;

    public SessionListModel() {
        super();

        mGroupInfoProvider = new GroupInfoProvider();
        mCurrentGroups = new GroupsList();
    }

    public GroupsList getGroups() {
        return mCurrentGroups;
    }

    public void setGroupType(GroupType groupType) {
        mGroupInfoProvider.setCurrentGroupType(groupType);
    }
}
