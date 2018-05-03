package mvasoft.timetracker.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.Session;
import mvasoft.timetracker.deprecated.SessionHelper;

public interface DataRepository {

    LiveData<GroupsList> getGroups(GroupType groupType);
    LiveData<Integer> deleteGroups(GroupType groupType, List<Long> groupIds);

    LiveData<Long> getOpenedSessionId();
    MutableLiveData<SessionHelper.ToggleSessionResult> toggleSession();

    LiveData<Boolean> updateSession(Session session);
    LiveData<Session> getSessionById(long id);
}
