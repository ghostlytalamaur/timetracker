package mvasoft.timetracker.vo;

import android.arch.persistence.room.Embedded;

public class SessionWithDescription extends Session {
    @Embedded
    private final DayDescription mDayDescription;

    public SessionWithDescription(long id, long startTime, long endTime, DayDescription dayDescription) {
        super(id, startTime, endTime);
        mDayDescription = dayDescription;
    }

    public DayDescription getDayDescription() {
        return mDayDescription;
    }
}
