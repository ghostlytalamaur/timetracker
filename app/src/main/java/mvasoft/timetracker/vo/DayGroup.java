package mvasoft.timetracker.vo;

import org.joda.time.DateTime;

import java.util.List;

import mvasoft.timetracker.preferences.AppPreferences;

public class DayGroup implements TimeInfoProvider {

    private final DayDescription mDayDescription;
    private final List<Session> mSessions;

    private long mDay;

    public DayGroup(long day, DayDescription dayDescription, List<Session> sessions) {
        mDay = day;
        mDayDescription = dayDescription;
        mSessions = sessions;
    }

    public void appendSessionIds(final List<Long> destIds) {
        if (destIds != null && mSessions != null)
            for (Session s : mSessions)
                if (!destIds.contains(s.getId()))
                    destIds.add(s.getId());
    }

    public boolean hasRunningSessions() {
        if (mSessions != null)
            for (Session s : mSessions)
                if (s.isRunning())
                    return true;
        return false;
    }

    public long getDay() {
        return mDay;
    }

    @Override
    public long getId() {
        return mDay;
    }

    @Override
    public long getStartTime() {
        long res = 0;
        if (mSessions.size() > 0)
            res = mSessions.get(0).getEndTime();

        for (Session s : mSessions)
            res = Math.max(res, s.getEndTime());
        return res;
    }

    @Override
    public long getEndTime() {
        long res = 0;
        if (mSessions.size() > 0)
            res = mSessions.get(0).getStartTime();

        for (Session s : mSessions)
            res = Math.min(res, s.getStartTime());
        return res;
    }


    public long getDuration() {
        long res = 0;
        if (mSessions != null)
            for (Session s : mSessions)
                res += s.getDuration();
        return res;
    }

    @Override
    public boolean isRunning() {
        if (mSessions != null)
            for (Session s : mSessions)
                if (s.isRunning())
                    return true;
        return false;
    }


    /**
     * return target duration in seconds
     * @param preferences preference instance
     * @return target duration
     */
    public long getTargetTime(final AppPreferences preferences) {
        boolean isWorkingDay = (mDayDescription != null && mDayDescription.isWorkingDay()) ||
                preferences.isWorkingDay(new DateTime(mDay * 1000).getDayOfWeek());

        if (!isWorkingDay)
            return 0;
        else if (mDayDescription != null)
            return mDayDescription.getTargetDuration() * 60;
        else
            return preferences.getTargetTimeInMin() * 60;
    }

    public List<Session> getSessions() {
        return mSessions;
    }

    public long getTargetTimeDiff(AppPreferences preferences) {
        return getDuration() - getTargetTime(preferences);
    }
}
