package mvasoft.timetracker.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "days")
public class DayDescription {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "dayId")
    private long mId;

    @ColumnInfo(name = "dayDate")
    private long mDate;

    @ColumnInfo(name = "targetDuration")
    private long mTargetDuration;

    @ColumnInfo(name = "isWorkingDay")
    private boolean mIsWorkingDay;

    public DayDescription(long id, long date, long targetDuration, boolean isWorkingDay) {
        mId = id;
        mDate = date;
        mTargetDuration = targetDuration;
        mIsWorkingDay = isWorkingDay;
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

    public boolean isWorkingDay() {
        return mIsWorkingDay;
    }
}
