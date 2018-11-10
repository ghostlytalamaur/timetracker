package mvasoft.timetracker.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import android.support.v4.util.LongSparseArray;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;
import timber.log.Timber;


@Dao
public abstract class SessionsDao {

    // perform any date/time comparison using 'localtime' modifier.
    // unixTime always is number of second in GTM0 timezone.
    // should adjust this unitTime to local time

    @Query("SELECT _id FROM sessions WHERE EndTime = 0 or EndTime IS NULL")
    public abstract Flowable<Long> getOpenedSessionIdRx();

    @Query("SELECT _id FROM sessions WHERE EndTime = 0 or EndTime IS NULL")
    public abstract Flowable<List<Long>> getOpenedSessionsIds();

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

    @Query("SELECT * from sessions WHERE date(StartTime, 'unixepoch', 'localtime') = date(:date, 'unixepoch', 'localtime')")
    public abstract List<Session> getSessionForDateRaw(long date);

    @Query("SELECT _id from sessions ORDER BY startTime DESC")
    public abstract Flowable<List<Long>> getSessionsIdsRx();

    @Insert
    public abstract long appendDayDescription(DayDescription dayDescription);

    @Update
    public abstract int updateDayDescription(DayDescription dayDescription);


    @Query("SELECT * from days WHERE date(:date, 'unixepoch', 'localtime') = date(dayDate, 'unixepoch', 'localtime')")
    public abstract Flowable<DayDescription> getDayDescriptionRx(long date);

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
        Flowable<List<DayDescription>> dayDescriptionsFlowable =
                getDayDescriptionsForDays(days.get(0), days.get(days.size() - 1));
        return Flowable
                .combineLatest(sessionsFlowable, dayDescriptionsFlowable, Pair::new)
                .map(daysAndSessions ->
                        buildGroups(daysAndSessions.first, daysAndSessions.second));
    }

    private static List<DayGroup> buildGroups(List<Session> sessions, List<DayDescription> days) {

        LongSparseArray<MutablePair<DayDescription, List<Session>>> map = new LongSparseArray<>();
        if (sessions != null)
            for (Session s : sessions) {
                long day = DateTimeHelper.startOfDay(s.getStartTime());

                MutablePair<DayDescription, List<Session>> pair = map.get(day);
                if (pair == null) {
                    pair = new MutablePair<>();
                    pair.second = new ArrayList<>();
                    map.put(day, pair);
                }

                pair.second.add(s);
            }

        if (days != null)
            for (DayDescription dd : days) {
                long day = DateTimeHelper.startOfDay(dd.getDate());
                MutablePair<DayDescription, List<Session>> pair = map.get(day);
                if (pair == null) {
                    pair = new MutablePair<>();
                    map.put(day, pair);
                }

                if (pair.first != null)
                    Timber.e("DayDescription already set for day %d", day);
                pair.first = dd;
            }


        List<DayGroup> resList = new ArrayList<>(map.size());
        for (int i = 0; i < map.size(); i++) {
            long day = map.keyAt(i);
            MutablePair<DayDescription, List<Session>> v = map.get(day);
            resList.add(new DayGroup(day, v.first, v.second));
        }

        return resList;
    }



    @Insert
    public abstract void appendAll(ArrayList<Session> list);


    private static class MutablePair<F, S> {
        F first;
        S second;
    }

//    @RawQuery("UPDATE sessions SET EndTime = strftime('%s', 'now') WHERE EndTime = 0 OR EndTime IS NULL; INSERT INTO sessions (StartTime) SELECT strftime('%s', 'now') WHERE (SELECT Changes() = 0);")
//    public abstract void toggleOpenedSession();
}
