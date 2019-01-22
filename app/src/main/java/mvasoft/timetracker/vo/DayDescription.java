package mvasoft.timetracker.vo;

import com.google.gson.annotations.SerializedName;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import mvasoft.timetracker.utils.DateTimeHelper;

@Entity(tableName = "days")
public class DayDescription {
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "dayId")
    private final long mId;

    @SerializedName("date")
    @ColumnInfo(name = "dayDate")
    private final long mDate;

    @SerializedName("targetDuration")
    @ColumnInfo(name = "targetDuration")
    private final long mTargetDuration;

    @SerializedName("isWorkingDay")
    @ColumnInfo(name = "isWorkingDay")
    private final boolean mIsWorkingDay;

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

    /**
     *
     * @return target duration for day in minutes
     */
    public long getTargetDuration() {
        return mTargetDuration;
    }

    public boolean isWorkingDay() {
        return mIsWorkingDay;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        DayDescription d = (DayDescription) obj;
        return (mId == d.mId) &&
                DateTimeHelper.sameDays(mDate, d.mDate) &&
                (mTargetDuration == d.mTargetDuration) &&
                (mIsWorkingDay == d.mIsWorkingDay);
    }
}
