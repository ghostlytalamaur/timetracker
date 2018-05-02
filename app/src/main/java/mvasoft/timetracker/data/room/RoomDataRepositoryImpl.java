package mvasoft.timetracker.data.room;

import android.arch.lifecycle.LiveData;

import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.data.DataRepository;

public class RoomDataRepositoryImpl implements DataRepository {

    @Override
    public LiveData<GroupsList> getGroups(GroupType groupType) {
        return null;
    }
}
