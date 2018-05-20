package mvasoft.timetracker.ui.editsession.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.vo.Session;

// TODO: 04.05.2018 recator this class
public class SessionEditModel {

    private final DataRepository mRepository;
    private long mSessionId;
    private LiveData<Session> mSessionLiveData;
    private MediatorLiveData<Long> mStartTimeData;
    private MediatorLiveData<Long> mEndTimeData;
    private MediatorLiveData<Long> mDurationData;
    private MediatorLiveData<Boolean> mIsRunningData;
    private MediatorLiveData<Boolean> mIsChangedData;
    private boolean mInRestoreState = false;

    public SessionEditModel(DataRepository repository) {
        super();
        mRepository = repository;

        mStartTimeData = new MediatorLiveData<>();
        mEndTimeData = new MediatorLiveData<>();
        mDurationData = new MediatorLiveData<>();
        mIsRunningData = new MediatorLiveData<>();
        mIsChangedData = new MediatorLiveData<>();

        mDurationData.addSource(mStartTimeData, start -> {
            if (start != null && mEndTimeData.getValue() != null)
                mDurationData.setValue(mEndTimeData.getValue() - start);
            else
                mDurationData.setValue((long) 0);
        });
        mDurationData.addSource(mEndTimeData, end -> {
            if (end != null && mStartTimeData.getValue() != null)
                mDurationData.setValue(end - mStartTimeData.getValue());
            else
                mDurationData.setValue((long) 0);
        });

        mIsChangedData.addSource(mStartTimeData, aStartTime -> {
            mIsChangedData.setValue(calculatedIsChanged());
        });

        mIsChangedData.addSource(mEndTimeData, aEndTime -> {
            mIsChangedData.setValue(calculatedIsChanged());
        });

        mIsChangedData.addSource(mIsRunningData, aIsRunning -> {
            mIsChangedData.setValue(calculatedIsChanged());
        });
    }

    private boolean calculatedIsChanged() {
        Session s = mSessionLiveData.getValue();
        if (s == null)
            return true;

        long start = mStartTimeData.getValue() != null ? mStartTimeData.getValue() : s.getStartTime();
        long end = mEndTimeData.getValue() != null ? mEndTimeData.getValue() : s.getEndTime();
        boolean isRunning = mIsRunningData.getValue() != null ? mIsRunningData.getValue() : s.isRunning();
        return (s.getStartTime() != start) ||
                (s.getEndTime() != end) ||
                (s.isRunning() != isRunning);
    }

    public void setId(long id) {
        if (id == mSessionId)
            return;

        if (mSessionId != 0) {
            mStartTimeData.setValue(null);
            mEndTimeData.setValue(null);
            mIsRunningData.setValue(null);
        }

        mSessionId = id;
        mSessionLiveData = mRepository.getSessionById(mSessionId);
        mStartTimeData.addSource(mSessionLiveData, session -> {
            if (!isChanged() || mStartTimeData.getValue() == null)
                mStartTimeData.setValue(session != null ? session.getStartTime() : 0);
        });
        mEndTimeData.addSource(mSessionLiveData, session -> {
            if (!isChanged() || mEndTimeData.getValue() == null) {
                mEndTimeData.setValue(session != null ? session.getEndTime() : 0);
//                mIsRunningData.setValue(session != null && session.getEndTime() == 0);
            }
        });
        mIsRunningData.addSource(mSessionLiveData, session -> {
            if (!isChanged() || mIsRunningData.getValue() == null)
                mIsRunningData.setValue(session == null || session.getEndTime() == 0);
        });
        mIsChangedData.addSource(mSessionLiveData, session ->
                mIsChangedData.setValue(calculatedIsChanged()));
    }

    public LiveData<Long> getStartData() {
        return mStartTimeData;
    }

    public LiveData<Long> getEndData() {
        return mEndTimeData;
    }

    public LiveData<Long> getDurationData() {
        return mDurationData;
    }

    public LiveData<Boolean> getIsChangedData() {
        return mIsChangedData;
    }

    public MutableLiveData<Boolean> getIsRunningData() {
        return mIsRunningData;
    }

    public void setStartTime(long start) {
        if (mStartTimeData.getValue() != null && mStartTimeData.getValue() == start)
            return;

        mStartTimeData.setValue(start);
    }

    public void setEndTime(long end) {
        if (mEndTimeData.getValue() != null && mEndTimeData.getValue() == end)
            return;

        mEndTimeData.setValue(end);
    }

    private boolean isChanged() {
        return mIsChangedData.getValue() != null && mIsChangedData.getValue();
    }


    public LiveData<Session> getSession() {
        return mSessionLiveData;
    }

    private boolean isRunning() {
        return mIsRunningData.getValue() == null || mIsRunningData.getValue();
    }

    public void safeSession() {
        Session s = mSessionLiveData.getValue();
        if (s == null)
            return;

        long start = mStartTimeData.getValue() != null ? mStartTimeData.getValue() : System.currentTimeMillis() / 1000;
        long end = 0;
        if (!isRunning())  {
            if (mEndTimeData.getValue() != null)
                end = mEndTimeData.getValue();
            else
                end = System.currentTimeMillis() / 1000;
        }


        mRepository.updateSession(new Session(s.getId(), start, end));
    }

    public void restoreState(Bundle state) {
        // TODO: check is data changed

        SavedState data = state.getParcelable("SESSION_EDIT_MODEL_SAVED_STATE");
        Objects.requireNonNull(data).restore(this);
    }

    public void saveState(Bundle outState) {
        outState.putParcelable("SESSION_EDIT_MODEL_SAVED_STATE", new SavedState(this));
    }

    public long getSessionId() {
        return mSessionId;
    }


    static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private long sessionId;
        private long startTime;
        private long endTime;
        private byte isRunning;

        SavedState(SessionEditModel data) {
            sessionId = data.mSessionId;
            startTime = data.mStartTimeData.getValue() != null ? data.mStartTimeData.getValue() : -1;
            endTime = data.mEndTimeData.getValue() != null ? data.mEndTimeData.getValue() : -1;
            if (data.mIsRunningData.getValue() != null)
                isRunning = (byte) (data.mIsRunningData.getValue() ? 1 : 0);
            else
                isRunning = 3;
        }

        private SavedState(Parcel in) {
            sessionId = in.readLong();
            startTime = in.readLong();
            endTime = in.readLong();
            isRunning = in.readByte();
        }

        void restore(SessionEditModel dest) {
            dest.mInRestoreState = true;
            try {
                dest.mStartTimeData.setValue(startTime > 0 ? startTime : null);
                dest.mEndTimeData.setValue(endTime > 0 ? endTime : null);
                if (isRunning == 3)
                    dest.mIsRunningData.setValue(null);
                else
                    dest.mIsRunningData.setValue(isRunning == 1);
                dest.mIsChangedData.setValue(false);
                dest.setId(sessionId);
            } finally {
                dest.mInRestoreState = false;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(sessionId);
            dest.writeLong(startTime);
            dest.writeLong(endTime);
            dest.writeByte(isRunning);
        }
    }


}