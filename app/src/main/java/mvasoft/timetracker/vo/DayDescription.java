package mvasoft.timetracker.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "days")
public class DayDescription {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long mId;

    @ColumnInfo(name = "date")
    private long mDate;

    @ColumnInfo(name = "targetDuration")
    private long mTargetDuration;

    public DayDescription(long id, long date, long targetDuration) {
        mId = id;
        mDate = date;
        mTargetDuration = targetDuration;
    }

    public long getId() {
        return mId;
    }

    public long getDate() {
        return mDate;
    }

    public long getTargetDuration() {
        return mTargetDuration;
    }
}
