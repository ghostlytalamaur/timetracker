package mvasoft.timetracker.vo;

import com.google.gson.annotations.SerializedName;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions")
public class Session {

    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private final long mId;

    @SerializedName("start")
    @ColumnInfo(name = "startTime")
    private final long mStartTime;

    @SerializedName("end")
    @ColumnInfo(name = "endTime")
    private final long mEndTime;

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

    public long getEndTime() {
        return mEndTime;
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
