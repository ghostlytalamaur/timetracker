package mvasoft.timetracker.data;

import android.arch.lifecycle.LiveData;

import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;

public interface DataRepository {
    LiveData<GroupsList> getGroups(GroupType groupType);
}
