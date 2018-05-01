package mvasoft.timetracker.extlist.model;

import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;

public class ExSessionListModel {

    private GroupType mGroupType;
    private GroupsList mGroups;

    public ExSessionListModel(GroupType groupType) {
        mGroupType = groupType;
        mGroups = new GroupsList();
    }

    public GroupsList getGroups() {
        return mGroups;
    }

    public GroupType getGroupType() {
        return mGroupType;
    }
}
