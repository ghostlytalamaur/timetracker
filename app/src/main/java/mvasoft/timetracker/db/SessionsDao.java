package mvasoft.timetracker.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mvasoft.timetracker.vo.Session;


@Dao
public abstract class SessionsDao {

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    public abstract LiveData<List<Session>> getAll();

//    @TypeConverters({RoomTypeConverters.class})
//    @Transaction
//    @Query("SELECT * FROM (SELECT min(_id) as groupId, datetime(min(StartTime), 'unixepoch') as groupStartTime, datetime(max(EndTime), 'unixepoch') as groupEndTime, group_concat(_id, ' ') as sessionIds FROM sessions GROUP BY date(StartTime, 'unixepoch', 'start of year'));")
//    public abstract LiveData<List<SessionGroupEntity>> getYearGroups();

    @Query("SELECT _id FROM sessions WHERE EndTime = 0 or EndTime IS NULL")
    public abstract LiveData<Long> getOpenedSessionId();

    @Query("DELETE FROM sessions WHERE _id in (:groupIds)")
    public abstract int deleteByIds(List<Long> groupIds);

    @Query("UPDATE sessions SET EndTime = strftime('%s', 'now', 'localtime') WHERE EndTime = 0 OR EndTime IS NULL")
    public abstract int closeOpenedSessions();

    @Query("SELECT * FROM sessions WHERE _id = :id")
    public abstract LiveData<Session> getSessionById(long id);

    @Update
    public abstract int updateSession(Session session);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long appendSession(Session entity);

    @Query("SELECT * from sessions WHERE date(StartTime, 'unixepoch') = date(:date, 'unixepoch')")
    public abstract LiveData<List<Session>> getSessionForDate(long date);

    @Query("SELECT _id from sessions ORDER BY startTime DESC")
    public abstract LiveData<List<Long>> getSessionsIds();

//    @RawQuery("UPDATE sessions SET EndTime = strftime('%s', 'now') WHERE EndTime = 0 OR EndTime IS NULL; INSERT INTO sessions (StartTime) SELECT strftime('%s', 'now') WHERE (SELECT Changes() = 0);")
//    public abstract void toggleOpenedSession();
}