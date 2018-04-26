package mvasoft.timetracker;

import android.os.Bundle;

import java.util.EventListener;

import mvasoft.utils.Announcer;

public class SessionEditData {
    private static final String STATE_ID         = "session_id";
    private static final String STATE_START_TIME = "start_time";
    private static final String STATE_END_TIME   = "end_time";
    private static final String STATE_ORIGINAL_START_TIME = "original_start";
    private static final String STATE_ORIGINAL_END_TIME = "original_end";
    private static final String STATE_ORIGINAL_IS_CLOSED = "is_changed";
    private final Announcer<ISessionDataChangedListener> mAnnouncer;


    private long mSessionId;
    private long mStartTime;
    private long mEndTime;
    private long mOriginalStartTime;
    private long mOriginalEndTime;
    private boolean mIsClosed;

    public SessionEditData(long id, long start, long end) {
        super();
        mSessionId = id;
        mOriginalStartTime = start;
        mOriginalEndTime = end;
        mAnnouncer = new Announcer<>(ISessionDataChangedListener.class);
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
        if (getStartTime() == start)
            return;

        mStartTime = start;
        mAnnouncer.announce().dataChanged(SessionDataType.sdtStartTime);
    }

    public long getEndTime() {
        if (mEndTime > 0)
            return mEndTime;
        else
            return mOriginalEndTime;
    }

    public void setEndTime(long end) {
        if (getEndTime() == end)
            return;;

        mEndTime = end;
        mAnnouncer.announce().dataChanged(SessionDataType.sdtEndTime);
    }

    public void restoreState(Bundle state) {
        mSessionId = state.getLong(STATE_ID);
        mOriginalStartTime = state.getLong(STATE_ORIGINAL_START_TIME);
        mOriginalEndTime = state.getLong(STATE_ORIGINAL_END_TIME);
        mStartTime = state.getLong(STATE_START_TIME);
        mEndTime = state.getLong(STATE_END_TIME);
        mIsClosed = state.getBoolean(STATE_ORIGINAL_IS_CLOSED);

        mAnnouncer.announce().dataChanged(SessionDataType.sdtAll);
    }

    public void saveState(Bundle outState) {
        outState.putLong(STATE_ID, mSessionId);
        outState.putLong(STATE_ORIGINAL_START_TIME, mOriginalStartTime);
        outState.putLong(STATE_ORIGINAL_END_TIME, mOriginalEndTime);
        outState.putLong(STATE_START_TIME, mStartTime);
        outState.putLong(STATE_END_TIME, mEndTime);
        outState.putBoolean(STATE_ORIGINAL_IS_CLOSED, mIsClosed);
    }

    private long getOriginalEndTime() {
        return mOriginalEndTime;
    }

    public void setOriginalEndTime(long end) {
        mOriginalEndTime = end;
        mIsClosed = mOriginalEndTime > 0;

        mAnnouncer.announce().dataChanged(SessionDataType.sdtStartTime);
        mAnnouncer.announce().dataChanged(SessionDataType.sdtClosed);
    }

    private long getOriginalStartTime() {
        return mOriginalStartTime;
    }

    public void setOriginalStartTime(long start) {
        mOriginalStartTime = start;

        mAnnouncer.announce().dataChanged(SessionDataType.sdtStartTime);
    }

    public long getDuration() {
        if (getEndTime() > 0)
            return getEndTime() - getStartTime();
        else
            return 0;
    }

    public boolean isClosed() {
        return mIsClosed;
    }

    public void setIsClosed(boolean aIsClosed) {
        if (mIsClosed == aIsClosed)
            return;

        mIsClosed = aIsClosed;
        if (mIsClosed) {
            if (getEndTime() == 0)
                setEndTime(System.currentTimeMillis() / 1000L);
        }
        else if (getOriginalEndTime() == 0)
            setEndTime(0);

        mAnnouncer.announce().dataChanged(SessionDataType.sdtClosed);
    }

    public boolean isChanged() {
        return (getOriginalStartTime() != getStartTime()) ||
                (getOriginalEndTime() != getEndTime()) ||
                ((mOriginalEndTime > 0) != isClosed());
    }

    public void addDataChangedListener(ISessionDataChangedListener listener) {
        mAnnouncer.addListener(listener);
    }
    
    public enum SessionDataType {
        sdtAll,
        sdtStartTime,
        sdtEndTime,
        sdtClosed
    }
    
    public interface ISessionDataChangedListener extends EventListener {
        void dataChanged(SessionDataType SessionDataType);
    }
}