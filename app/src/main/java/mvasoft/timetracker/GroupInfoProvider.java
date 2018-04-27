package mvasoft.timetracker;

import android.net.Uri;

import mvasoft.timetracker.data.DatabaseDescription;

public class GroupInfoProvider {

    private GroupType mCurrentGroupType;

    Uri getCurrentGroupsUri() {
        switch (mCurrentGroupType) {
            case gt_None:
                return DatabaseDescription.GroupsDescription.GROUP_NONE_URI;
            case gt_Day:
                return DatabaseDescription.GroupsDescription.GROUP_DAY_URI;
            case gt_Week:
                return DatabaseDescription.GroupsDescription.GROUP_WEEK_URI;
            case gt_Month:
                return DatabaseDescription.GroupsDescription.GROUP_MONTH_URI;
            case gt_Year:
                return DatabaseDescription.GroupsDescription.GROUP_YEAR_URI;
        }

        return null;
    }

    GroupType getCurrentGroupType() {
        return mCurrentGroupType;
    }

    void setCurrentGroupType(GroupType groupType) {
        mCurrentGroupType = groupType;
    }
}
