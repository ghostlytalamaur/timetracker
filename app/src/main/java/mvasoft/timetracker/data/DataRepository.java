package mvasoft.timetracker.data;

import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

public interface DataRepository {

    LiveData<List<Session>> getSessions();
    LiveData<Integer> deleteSessions(List<Long> ids);

    LiveData<Long> getOpenedSessionId();
    LiveData<ToggleSessionResult> toggleSession();

    LiveData<Boolean> updateSession(Session session);
    LiveData<Session> getSessionById(long id);

    LiveData<List<Session>> getSessionForDate(long date);

    LiveData<List<Long>> getSessionsIds();

    LiveData<DayDescription> getDayDescription(Long date);

    /**
     * append new {@link DayDescription} or update existing if {@link DayDescription}'s id is set.
     * @param dayDescription
     */
    void updateDayDescription(DayDescription dayDescription);


    LiveData<List<DayGroup>> getDayGroups(List<Long> days);

    void appendAll(ArrayList<Session> list);

    enum ToggleSessionResult {
        tgs_Started,
        tgs_Stopped,
        tgs_Error
    }
}
