package mvasoft.timetracker.data.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import mvasoft.timetracker.Session;
import mvasoft.timetracker.data.room.entity.SessionGroupEntity;


@Dao
public abstract class GroupsListDao {

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    public abstract LiveData<List<SessionGroupEntity>> getAll();

    @Query("SELECT _id FROM sessions WHERE EndTime = 0 or EndTime IS NULL")
    public abstract LiveData<Long> getOpenedSessionId();

    @Query("DELETE FROM sessions WHERE _id in (:groupIds)")
    public abstract int deleteByIds(List<Long> groupIds);

    @Query("UPDATE sessions SET EndTime = strftime('%s', 'now', 'localtime') WHERE EndTime = 0 OR EndTime IS NULL")
    public abstract int closeOpenedSessions();

    @Query("SELECT * FROM sessions WHERE _id = :id")
    public abstract LiveData<Session> getSessionById(long id);

    @Update
    public abstract int updateSession(SessionGroupEntity session);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long appendSession(SessionGroupEntity entity);

//    @RawQuery("UPDATE sessions SET EndTime = strftime('%s', 'now') WHERE EndTime = 0 OR EndTime IS NULL; INSERT INTO sessions (StartTime) SELECT strftime('%s', 'now') WHERE (SELECT Changes() = 0);")
//    public abstract void toggleOpenedSession();
}
