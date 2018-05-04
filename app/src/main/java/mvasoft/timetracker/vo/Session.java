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
}
