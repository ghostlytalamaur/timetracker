package mvasoft.timetracker.model;

import mvasoft.timetracker.GroupInfoProvider;
import mvasoft.timetracker.GroupsList;

public class SessionListModel {

    private GroupInfoProvider mGroupInfoProvider;

    private GroupsList mCurrentGroups;
    private GroupsList mTodayGroup;
    private GroupsList mWeekGroup;

    public SessionListModel() {
        super();

        mGroupInfoProvider = new GroupInfoProvider();
        mTodayGroup = new GroupsList();
        mWeekGroup = new GroupsList();
        mCurrentGroups = new GroupsList();
    }

    public GroupsList getCurrentGroups() {
        return mCurrentGroups;
    }

    public GroupsList getTodayGroups() {
        return mTodayGroup;
    }

    public GroupsList getWeekGroups() {
        return mWeekGroup;
    }
}
