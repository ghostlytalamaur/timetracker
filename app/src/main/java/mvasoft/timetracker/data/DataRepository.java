package mvasoft.timetracker.data;

import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

public interface DataRepository {

    LiveData<List<Session>> getSessions();


    /**
     * Delete session with id in ids list.
     * Post number of deleted rows as event {@link mvasoft.timetracker.data.event.SessionsDeletedEvent}
     * @param ids session ids to delete
     */
    void deleteSessions(List<Long> ids);

    LiveData<Long> getOpenedSessionId();

    /**
     * Close opened session if it exists. <p>
     * Start new session if no opened sessions was found. <p>
     * Post result as event {@link mvasoft.timetracker.data.event.SessionToggledEvent}.
     */
    void toggleSession();

    /**
     * Save session to repository.
     * Post event {@link mvasoft.timetracker.data.event.SessionSavedEvent}
     * @param session - session to update
     */
    void updateSession(Session session);
    LiveData<Session> getSessionById(long id);

//    LiveData<List<Session>> getSessionForDate(long date);

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
