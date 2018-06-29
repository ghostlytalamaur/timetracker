package mvasoft.timetracker.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import android.util.Log;
import android.util.Pair;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;
import mvasoft.timetracker.vo.SessionWithDescription;


@Dao
public abstract class SessionsDao {

    private static final String LOGT = "mvasoft.log.dao";

    // perform any date/time comparison using 'localtime' modifier.
    // unixTime always is number of second in GTM0 timezone.
    // should adjust this unitTime to local time

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    public abstract LiveData<List<Session>> getAll();

//    @TypeConverters({RoomTypeConverters.class})
//    @Transaction
//    @Query("SELECT * FROM (SELECT min(_id) as groupId, datetime(min(StartTime), 'unixepoch', 'localtime') as groupStartTime, datetime(max(EndTime), 'unixepoch', 'localtime') as groupEndTime, group_concat(_id, ' ') as sessionIds FROM sessions GROUP BY date(StartTime, 'unixepoch', 'localtime', 'start of year'));")
//    public abstract LiveData<List<SessionGroupEntity>> getYearGroups();

    @Query("SELECT _id FROM sessions WHERE EndTime = 0 or EndTime IS NULL")
    public abstract LiveData<Long> getOpenedSessionId();

    @Query("DELETE FROM sessions WHERE _id in (:groupIds)")
    public abstract int deleteByIds(List<Long> groupIds);

    @Query("UPDATE sessions SET EndTime = strftime('%s', 'now') WHERE EndTime = 0 OR EndTime IS NULL")
    public abstract int closeOpenedSessions();

    @Query("SELECT * FROM sessions WHERE _id = :id")
    public abstract LiveData<Session> getSessionById(long id);
    @Query("SELECT * FROM sessions WHERE _id = :id")
    public abstract Flowable<Session> getSessionByIdRx(long id);

    @Update
    public abstract int updateSession(Session session);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long appendSession(Session entity);

//    LEFT JOIN days on date(startTime, 'unixepoch', 'localtime') = date(days.dayDate, 'unixepoch', 'localtime')
    @Query("SELECT * from sessions WHERE date(StartTime, 'unixepoch', 'localtime') = date(:date, 'unixepoch', 'localtime')")
    public abstract LiveData<List<Session>> getSessionForDate(long date);
    @Query("SELECT * from sessions WHERE date(StartTime, 'unixepoch', 'localtime') = date(:date, 'unixepoch', 'localtime')")
    public abstract List<Session> getSessionForDateRaw(long date);

    @Query("SELECT _id from sessions ORDER BY startTime DESC")
    public abstract LiveData<List<Long>> getSessionsIds();

    @Query("SELECT * from sessions LEFT JOIN days on date(startTime, 'unixepoch', 'localtime') = date(days.dayDate, 'unixepoch', 'localtime') WHERE startTime BETWEEN :dateStart AND :dateEnd")
    public abstract LiveData<List<SessionWithDescription>> getSessionWithDescription(long dateStart, long dateEnd);


    @Query("SELECT dayId from days WHERE date(:date, 'unixepoch', 'localtime') = date(dayDate, 'unixepoch', 'localtime')")
    public abstract boolean hasDayDescription(long date);

    @Insert
    public abstract long appendDayDescription(DayDescription dayDescription);

    @Update
    public abstract int updateDayDescription(DayDescription dayDescription);


    @Query("SELECT * from days WHERE date(:date, 'unixepoch', 'localtime') = date(dayDate, 'unixepoch', 'localtime')")
    public abstract Flowable<DayDescription> getDayDescriptionRx(long date);
    @Query("SELECT * from days WHERE date(:date, 'unixepoch', 'localtime') = date(dayDate, 'unixepoch', 'localtime')")
    public abstract LiveData<DayDescription> getDayDescription(long date);



    @Query("SELECT * from days WHERE date(:date, 'unixepoch', 'localtime') = date(dayDate, 'unixepoch', 'localtime')")
    public abstract DayDescription getDayDescriptionRaw(long date);

    @Transaction
    public List<DayGroup> getDayGroups(List<Long> days) {
        ArrayList<DayGroup> res = null;
        for (Long day : days) {
            DayDescription dayDescription = getDayDescriptionRaw(day);
            List<Session> sessions = getSessionForDateRaw(day);
            if (dayDescription == null && (sessions == null || sessions.size() == 0))
                continue;

            if (res == null)
                res = new ArrayList<>();
            res.add(new DayGroup(day, dayDescription, sessions == null || sessions.size() == 0 ? null : sessions));
        }
        return res;
    }

    @Query("SELECT * from sessions WHERE date(startTime, 'unixepoch', 'localtime') BETWEEN date(:start, 'unixepoch', 'localtime') AND date(:end, 'unixepoch', 'localtime')")
    abstract Flowable<List<Session>> getSessionsForDays(long start, long end);

    @Query("SELECT * from days WHERE date(dayDate, 'unixepoch', 'localtime') BETWEEN date(:start, 'unixepoch', 'localtime') AND date(:end, 'unixepoch', 'localtime')")
    abstract Flowable<List<DayDescription>> getDayDescriptionsForDays(long start, long end);

    public Flowable<List<DayGroup>> getDayGroupsRx(List<Long> days) {
        Flowable<List<Session>> sessionsFlowable =
                getSessionsForDays(days.get(0), days.get(days.size() - 1));

        int daysCount = days.size();
        sessionsFlowable = sessionsFlowable
                .doOnNext((list) -> {
                    Log.d(LOGT, String.format("%d days: %d sessions fetched.",
                            daysCount, list.size()));
                });

        Flowable<List<DayDescription>> dayDescriptionsFlowable =
                getDayDescriptionsForDays(days.get(0), days.get(days.size() - 1));

        dayDescriptionsFlowable = dayDescriptionsFlowable
                .doOnNext((list) -> {
                    Log.d(LOGT, String.format("%d days: %d day descriptions fetched.",
                            daysCount, list.size()));
                });

        return Flowable
                .combineLatest(sessionsFlowable, dayDescriptionsFlowable, (sessions, dd) -> {
                    Log.d(LOGT, String.format("%d days: create pair.", daysCount));
                    return new Pair<>(sessions, dd);
                })
                .map(daysAndSessions -> {
                    Log.d(LOGT, String.format("%d days: building groups", daysCount));
                    Map<Long, Collection<Session>> multimap;
                    if (daysAndSessions.first != null)
                        multimap = Multimaps.index(daysAndSessions.first,
                                session -> DateTimeHelper.startOfDay(session.getStartTime()))
                                .asMap();
                    else
                        multimap = new HashMap<>();

                    Map<Long, DayDescription> dayDescriptionsMap;
                    if (daysAndSessions.second != null)
                        dayDescriptionsMap =
                                Maps.uniqueIndex(daysAndSessions.second,
                                        dd -> DateTimeHelper.startOfDay(dd.getDate()));
                    else
                        dayDescriptionsMap = new HashMap<>();

                    List<DayGroup> groups = new ArrayList<>();
                    for (Long day : Iterables.concat(multimap.keySet(), dayDescriptionsMap.keySet())) {
                        List<Session> sessions = null;
                        if (multimap.containsKey(day))
                            sessions = new ArrayList<>(multimap.get(day));
                        DayDescription dd = null;
                        if (dayDescriptionsMap.containsKey(day))
                            dd = dayDescriptionsMap.get(day);

                        groups.add(new DayGroup(day, dd, sessions));
                    }

                    Log.d(LOGT, String.format("%d days: %d groups was builded.",
                            daysCount, groups.size()));
                    return groups;
                });
    }




    @Insert
    public abstract void appendAll(ArrayList<Session> list);

    /*
        working with DayGroup
     */


//    @RawQuery("UPDATE sessions SET EndTime = strftime('%s', 'now') WHERE EndTime = 0 OR EndTime IS NULL; INSERT INTO sessions (StartTime) SELECT strftime('%s', 'now') WHERE (SELECT Changes() = 0);")
//    public abstract void toggleOpenedSession();
}
