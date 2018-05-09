package mvasoft.timetracker.vo;

import android.arch.persistence.room.Embedded;

import java.util.List;

public class DayGroup {

    @Embedded
    private DayDescription mDayDescription;
    @Embedded
    private List<Session> mSessions;

    public DayGroup(DayDescription dayDescription, List<Session> sessions) {
        mDayDescription = dayDescription;
        mSessions = sessions;
    }

    //    long getStartTime() {
//        long res = 0;
//        if (mItems.size() > 0)
//            res = mItems.get(0).getStartTime();
//
//        for (Session s : mItems)
//            res = Math.min(res, s.getStartTime());
//        return res;
//    }
//
//    long getEndTime() {
//        long res = 0;
//        if (mItems.size() > 0)
//            res = mItems.get(0).getEndTime();
//
//        for (Session s : mItems)
//            res = Math.max(res, s.getEndTime());
//        return res;
//    }
//
//    long getDuration() {
//        long res = 0;
//        for (Session s : mItems)
//            res += s.getDuration();
//        return res;
//    }
//
//    long getTargetDiff() {
//        long res = 0;
//        for (Session s : mItems)
//            res += s.getTargetDiff();
//        return res;
//    }
}
