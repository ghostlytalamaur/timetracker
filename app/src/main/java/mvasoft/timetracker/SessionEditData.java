package mvasoft.timetracker;

import android.os.Bundle;

public class SessionEditData {
    private static final String STATE_ID         = "session_id";
    private static final String STATE_START_TIME = "start_time";
    private static final String STATE_END_TIME   = "end_time";
    private static final String STATE_ORIGINAL_START_TIME = "original_start";
    private static final String STATE_ORIGINAL_END_TIME = "original_end";


    private long mSessionId;
    private long mStartTime;
    private long mEndTime;
    private long mOriginalStartTime;
    private long mOriginalEndTime;

    public SessionEditData(long id, long start, long end) {
        super();
        mSessionId = id;
        mOriginalStartTime = start;
        mOriginalEndTime = end;
    }

    public long getId() {
        return mSessionId;
    }

    public void setId(long id) {
        mSessionId = id;
    }

    public long getStartTime() {
        if (mStartTime > 0)
            return mStartTime;
        else
            return mOriginalStartTime;
    }

    public void setStartTime(long start) {
        mStartTime = start;
    }

    public long getEndTime() {
        if (mEndTime > 0)
            return mEndTime;
        else
            return mOriginalEndTime;
    }

    public void setEndTime(long end) {
        mEndTime = end;
    }

    public void restoreState(Bundle state) {
        mSessionId = state.getLong(STATE_ID);
        mOriginalStartTime = state.getLong(STATE_ORIGINAL_START_TIME);
        mOriginalEndTime = state.getLong(STATE_ORIGINAL_END_TIME);
        mStartTime = state.getLong(STATE_START_TIME);
        mEndTime = state.getLong(STATE_END_TIME);
    }

    public void saveState(Bundle outState) {
        outState.putLong(STATE_ID, mSessionId);
        outState.putLong(STATE_ORIGINAL_START_TIME, mOriginalStartTime);
        outState.putLong(STATE_ORIGINAL_END_TIME, mOriginalEndTime);
        outState.putLong(STATE_START_TIME, mStartTime);
        outState.putLong(STATE_END_TIME, mEndTime);
    }

    public long getOriginalEndTime() {
        return mOriginalEndTime;
    }

    public void setOriginalEndTime(long end) {
        mOriginalEndTime = end;
    }

    public long getOriginalStartTime() {
        return mOriginalStartTime;
    }

    public void setOriginalStartTime(long start) {
        mOriginalStartTime = start;
    }

    public long getDuration() {
        if (getEndTime() > 0)
            return getEndTime() - getStartTime();
        else
            return 0;
    }

    public boolean isClosed() {
        return getEndTime() > 0;
    }

    public boolean isChanged() {
        return (getOriginalStartTime() != getStartTime()) ||
                (getOriginalEndTime() != getEndTime());
    }
}