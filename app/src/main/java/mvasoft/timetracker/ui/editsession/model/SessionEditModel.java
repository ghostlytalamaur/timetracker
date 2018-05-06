package mvasoft.timetracker.ui.editsession.model;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.EventListener;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.vo.Session;
import mvasoft.utils.Announcer;

// TODO: 04.05.2018 recator this class
public class SessionEditModel {
    private static final String STATE_ID         = "session_id";
    private static final String STATE_START_TIME = "start_time";
    private static final String STATE_END_TIME   = "end_time";
    private static final String STATE_ORIGINAL_START_TIME = "original_start";
    private static final String STATE_ORIGINAL_END_TIME = "original_end";
    private static final String STATE_ORIGINAL_IS_CLOSED = "is_changed";
    private static final int FLAGS_FALSE = 0;
    private static final int FLAGS_TRUE = 1;
    private static final int FLAGS_UNDEF = -1;

    private final Announcer<ISessionDataChangedListener> mAnnouncer;
    private final DataRepository mRepository;
    private long mSessionId;
    private long mStartTime;
    private long mEndTime;
    private long mOriginalStartTime;
    private long mOriginalEndTime;
    private int mIsClosed;
    private LiveData<Session> mSessionLiveData;

    public SessionEditModel(DataRepository repository) {
        super();
        mRepository = repository;
        mAnnouncer = new Announcer<>(ISessionDataChangedListener.class);
        mIsClosed = FLAGS_UNDEF;
    }

    public long getId() {
        return mSessionId;
    }

    public void setId(long id) {
        if (id == mSessionId)
            return;

        mSessionId = id;
        mSessionLiveData = mRepository.getSessionById(mSessionId);
        mSessionLiveData.observeForever(new SessionObserver());
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
            return;

        mEndTime = end;
        mAnnouncer.announce().dataChanged(SessionDataType.sdtEndTime);
    }

    public void restoreState(Bundle state) {
        // TODO: check is data changed
        mSessionId = state.getLong(STATE_ID, mSessionId);
        mOriginalStartTime = state.getLong(STATE_ORIGINAL_START_TIME, mOriginalStartTime);
        mOriginalEndTime = state.getLong(STATE_ORIGINAL_END_TIME, mOriginalEndTime);
        mStartTime = state.getLong(STATE_START_TIME, mStartTime);
        mEndTime = state.getLong(STATE_END_TIME, mEndTime);
        mIsClosed = state.getInt(STATE_ORIGINAL_IS_CLOSED, mIsClosed);

        mAnnouncer.announce().dataChanged(SessionDataType.sdtAll);
    }

    public void saveState(Bundle outState) {
        outState.putLong(STATE_ID, mSessionId);
        outState.putLong(STATE_ORIGINAL_START_TIME, mOriginalStartTime);
        outState.putLong(STATE_ORIGINAL_END_TIME, mOriginalEndTime);
        outState.putLong(STATE_START_TIME, mStartTime);
        outState.putLong(STATE_END_TIME, mEndTime);
        outState.putInt(STATE_ORIGINAL_IS_CLOSED, mIsClosed);
    }

    private long getOriginalEndTime() {
        return mOriginalEndTime;
    }

    private void setOriginalEndTime(long end) {
        if (mOriginalEndTime == end)
            return;

        mOriginalEndTime = end;
        mIsClosed = mOriginalEndTime > 0 ? FLAGS_TRUE : FLAGS_FALSE;

        mAnnouncer.announce().dataChanged(SessionDataType.sdtStartTime);
        mAnnouncer.announce().dataChanged(SessionDataType.sdtClosed);
    }

    private long getOriginalStartTime() {
        return mOriginalStartTime;
    }

    private void setOriginalStartTime(long start) {
        if (mOriginalStartTime == start)
            return;

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
        return mIsClosed == FLAGS_TRUE;
    }

    public void setIsClosed(boolean aIsClosed) {
        if (isClosed() == aIsClosed)
            return;

        mIsClosed = aIsClosed ? FLAGS_TRUE : FLAGS_FALSE;
        if (isClosed()) {
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

    public LiveData<Session> getSession() {
        return mSessionLiveData;
    }

    public Session getSessionForUpdate() {
        Session s = mSessionLiveData.getValue();
        if (s != null)
            return new Session(s.getId(),getStartTime(), isClosed() ? getEndTime() : 0);
        else
            return null;
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

    private class SessionObserver implements android.arch.lifecycle.Observer<Session> {
        @Override
        public void onChanged(@Nullable Session session) {
            if (session != null) {
                setOriginalStartTime(session.getStartTime());
                setOriginalEndTime(session.getEndTime());
                mSessionId = session.getId();
            }
            else {
                setOriginalStartTime(0);
                setOriginalEndTime(0);
                mSessionId = -1;
            }
        }
    }
}