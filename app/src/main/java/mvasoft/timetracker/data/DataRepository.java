package mvasoft.timetracker.data;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import mvasoft.timetracker.events.SessionSavedEvent;
import mvasoft.timetracker.events.SessionToggledEvent;
import mvasoft.timetracker.events.SessionsDeletedEvent;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

public interface DataRepository {

    /**
     * Delete session with id in ids list.
     * Post number of deleted rows as event {@link SessionsDeletedEvent}
     * @param ids session ids to delete
     */
    void deleteSessions(List<Long> ids);

    Flowable<List<Long>> getOpenedSessionsIds();

    /**
     * Close opened session if it exists. <p>
     * Start new session if no opened sessions was found. <p>
     * Post result as event {@link SessionToggledEvent}.
     */
    void toggleSession();

    /**
     * Save session to repository.
     * Post event {@link SessionSavedEvent}
     * @param session - session to update
     */
    void updateSession(Session session);
    LiveData<Session> getSessionById(long id);
    Flowable<List<Long>> getSessionsIdsRx();


    /**
     * append new {@link DayDescription} or update existing if {@link DayDescription}'s id is set.
     * @param dayDescription description for day
     */
    void updateDayDescription(DayDescription dayDescription);

    Flowable<List<Session>> getSessionsRx(long start, long end);
    Flowable<List<DayGroup>> getDayGroupsRx(List<Long> days);
    Flowable<DayDescription> getDayDescriptionRx(Long date);
    Flowable<List<DayDescription>> getDayDescriptionsRx(Long start, Long end);


    void appendAll(ArrayList<Session> list);


    enum ToggleSessionResult {
        tgs_Started,
        tgs_Stopped,
        tgs_Error
    }
}
