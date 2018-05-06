package mvasoft.timetracker.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import mvasoft.timetracker.vo.Session;

public interface DataRepository {

    LiveData<List<Session>> getSessions();
    LiveData<Integer> deleteSessions(List<Long> ids);

    LiveData<Long> getOpenedSessionId();
    MutableLiveData<ToggleSessionResult> toggleSession();

    LiveData<Boolean> updateSession(Session session);
    LiveData<Session> getSessionById(long id);

    LiveData<List<Session>> getSessionForDate(long date);

    LiveData<List<Long>> getSessionsIds();

    enum ToggleSessionResult {
        tgs_Started,
        tgs_Stopped,
        tgs_Error
    }
}
