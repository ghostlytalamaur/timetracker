package mvasoft.timetracker.vo;

import org.joda.time.DateTime;

import java.util.List;

import mvasoft.timetracker.preferences.AppPreferences;

public class DayGroup implements TimeInfoProvider {

    private final DayDescription mDayDescription;
    private final List<Session> mSessions;
    private final boolean mIsRunning;
    private final long mStartTime;
    private final long mEndTime;
    private final long mDuration;

    private long mDay;

    public DayGroup(long day, DayDescription dayDescription, List<Session> sessions) {
        mDay = day;
        mDayDescription = dayDescription;
        mSessions = sessions;

        boolean isRunning = false;
        long start = 0;
        long end = 0;
        long duration = 0;
        if (mSessions != null && mSessions.size() > 0) {
            start = mSessions.get(0).getStartTime();
            end = mSessions.get(0).getEndTime();
            for (Session s : mSessions) {
                isRunning = isRunning || s.isRunning();
                start = Math.min(start, s.getStartTime());
                end = Math.max(end, s.getEndTime());
                duration += s.getDuration();
            }
        }

        mIsRunning = isRunning;
        mStartTime = start;
        mEndTime = isRunning ? 0 : end;
        mDuration = isRunning ? 0 : duration;
    }

    public void appendSessionIds(final List<Long> destIds) {
        if (destIds != null && mSessions != null)
            for (Session s : mSessions)
                if (!destIds.contains(s.getId()))
                    destIds.add(s.getId());
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
        return mStartTime;
    }

    @Override
    public long getEndTime() {
        return mEndTime;
    }

    public boolean hasSessions() {
        return mSessions != null && mSessions.size() > 0;
    }

    public long getDuration() {
        long res = 0;

        if (mIsRunning) {
            if (mSessions != null)
                for (Session s : mSessions)
                    res += s.getDuration();
        }
        else
            res = mDuration;

        return res;
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
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
