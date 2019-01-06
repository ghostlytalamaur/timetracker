package mvasoft.timetracker.db;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;


@Dao
public abstract class SessionsDao {

    // perform any date/time comparison using 'localtime' modifier.
    // unixTime always is number of second in GTM0 timezone.
    // should adjust this unitTime to local time

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

    @Query("SELECT * from days WHERE date(dayDate, 'unixepoch', 'localtime') BETWEEN date(:start, 'unixepoch', 'localtime') AND date(:end, 'unixepoch', 'localtime')")
    public abstract Flowable<List<DayDescription>> getDayDescriptionsRx(long start, long end);

    @Query("SELECT * from sessions WHERE date(startTime, 'unixepoch', 'localtime') BETWEEN date(:start, 'unixepoch', 'localtime') AND date(:end, 'unixepoch', 'localtime')")
    public abstract Flowable<List<Session>> getSessionsRx(long start, long end);

    @Query("SELECT * from days WHERE date(dayDate, 'unixepoch', 'localtime') BETWEEN date(:start, 'unixepoch', 'localtime') AND date(:end, 'unixepoch', 'localtime')")
    abstract Flowable<List<DayDescription>> getDayDescriptionsForDays(long start, long end);

    @Insert
    public abstract void appendAll(ArrayList<Session> list);


    private static class MutablePair<F, S> {
        F first;
        S second;
    }

//    @RawQuery("UPDATE sessions SET EndTime = strftime('%s', 'now') WHERE EndTime = 0 OR EndTime IS NULL; INSERT INTO sessions (StartTime) SELECT strftime('%s', 'now') WHERE (SELECT Changes() = 0);")
//    public abstract void toggleOpenedSession();
}
