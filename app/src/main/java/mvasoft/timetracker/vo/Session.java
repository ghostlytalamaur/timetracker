package mvasoft.timetracker.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "sessions")
public class Session {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private final long mId;

    @ColumnInfo(name = "StartTime")
    private long mStartTime;

    @ColumnInfo(name = "EndTime")
    private long mEndTime;

    public Session(long id, long startTime, long endTime) {
        mId = id;
        mStartTime = startTime;
        mEndTime = endTime;
    }

    public long getId() {
        return mId;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long endTime) {
        mEndTime = endTime;
    }

    public boolean isRunning() {
        return mEndTime == 0;
    }

    public long getDuration() {
        if (mEndTime != 0)
            return mEndTime - mStartTime;
        else
            return System.currentTimeMillis() / 1000 - mStartTime;
    }

    public boolean isGoalAchieved() {
        return false;
    }

    public long getGoalTimeDiff() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        Session s = (Session) obj;
        return (mId == s.mId) &&
                (mStartTime == s.mStartTime) &&
                (mEndTime == s.mEndTime);
    }
}
