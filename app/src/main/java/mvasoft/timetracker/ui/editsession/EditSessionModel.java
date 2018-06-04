package mvasoft.timetracker.ui.editsession;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.Session;

public class EditSessionModel {

    private final DataRepository mRepository;
    private long mSessionId;
    private Session mSession;
    private LiveData<Session> mSessionLiveData;
    private MediatorLiveData<Long> mStartTimeData;
    private MediatorLiveData<Long> mEndTimeData;
    private MediatorLiveData<Long> mDurationData;
    private MediatorLiveData<Boolean> mIsRunningData;
    private MediatorLiveData<Boolean> mIsChangedData;

    public EditSessionModel(DataRepository repository) {
        super();
        mRepository = repository;

        mStartTimeData = new MediatorLiveData<>();
        mEndTimeData = new MediatorLiveData<>();
        mDurationData = new MediatorLiveData<>();
        mIsRunningData = new MediatorLiveData<>();
        mIsChangedData = new MediatorLiveData<>();

        mDurationData.addSource(mStartTimeData, start -> updateDuration());
        mDurationData.addSource(mEndTimeData, end -> updateDuration());
        mDurationData.addSource(mIsRunningData, isRunning -> updateDuration());
    }

    private boolean calculatedIsChanged() {
        Session s = mSession;
        if (s == null)
            return false;

        long start = mStartTimeData.getValue() != null ? mStartTimeData.getValue() : s.getStartTime();
        long end = mEndTimeData.getValue() != null ? mEndTimeData.getValue() : s.getEndTime();
        boolean isRunning = mIsRunningData.getValue() != null ? mIsRunningData.getValue() : s.isRunning();
        return (s.getStartTime() != start) ||
                (!isRunning && (s.getEndTime() != end)) ||
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

        // in that case session will be queried only when one of liveData activated
        mStartTimeData.addSource(mSessionLiveData, this::setSession);
        mEndTimeData.addSource(mSessionLiveData, this::setSession);
        mIsRunningData.addSource(mSessionLiveData, this::setSession);
        mIsChangedData.addSource(mSessionLiveData, this::setSession);
    }

    private void setSession(Session s) {
        if (mSession == s)
            return;

        if (!Objects.equals(s, mSession)) {
            mStartTimeData.setValue(s != null ? s.getStartTime() : null);
            mEndTimeData.setValue(s != null ? s.getEndTime() : null);
            mIsRunningData.setValue(s == null || s.getEndTime() == 0);
            mIsChangedData.setValue(false);
        }
        mSession = s;
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

    public LiveData<Boolean> getIsRunningData() {
        return mIsRunningData;
    }

    public LiveData<Session> getSession() {
        return mSessionLiveData;
    }

    private void setStartDateTime(long start) {
        if (mStartTimeData.getValue() != null && mStartTimeData.getValue() == start)
            return;

        mStartTimeData.setValue(start);
        mIsChangedData.setValue(calculatedIsChanged());
    }

    private void setEndDateTime(long end) {
        if (mEndTimeData.getValue() != null && mEndTimeData.getValue() == end)
            return;

        mEndTimeData.setValue(end);
        mIsChangedData.setValue(calculatedIsChanged());
    }

    public void setIsRunning(boolean isRunning) {
        if (mIsRunningData.getValue() != null && mIsRunningData.getValue() == isRunning)
            return;

        mIsRunningData.setValue(isRunning);
        if (mEndTimeData.getValue() == null || mEndTimeData.getValue() == 0)
            mEndTimeData.setValue(System.currentTimeMillis() / 1000);

        mIsChangedData.setValue(calculatedIsChanged());
    }

    public void setStartTime(int hourOfDay, int minute) {
        if (mStartTimeData.getValue() == null)
            return;

        setStartDateTime(DateTimeHelper.withTime(mStartTimeData.getValue(), hourOfDay, minute));
    }

    public void setEndTime(int hourOfDay, int minute) {
        if (mEndTimeData.getValue() == null)
            return;

        setEndDateTime(DateTimeHelper.withTime(mEndTimeData.getValue(), hourOfDay, minute));
    }

    public void setStartDate(int year, int month, int dayOfMonth) {
        if (mStartTimeData.getValue() == null)
            return;

        setStartDateTime(DateTimeHelper.withDate(mStartTimeData.getValue(), year, month, dayOfMonth));
    }

    public void setEndDate(int year, int month, int dayOfMonth) {
        if (mEndTimeData.getValue() == null)
            return;

        setEndDateTime(DateTimeHelper.withDate(mEndTimeData.getValue(), year, month, dayOfMonth));
    }

    public boolean isRunning() {
        return mIsRunningData.getValue() == null || mIsRunningData.getValue();
    }

    public long getSessionId() {
        return mSessionId;
    }

    public void updateDuration() {
        if (mStartTimeData.getValue() == null)
            return;

        long start = mStartTimeData.getValue();
        long end;
        if (isRunning() || mEndTimeData.getValue() == null)
            end = System.currentTimeMillis() / 1000;
        else
            end = mEndTimeData.getValue();

        mDurationData.postValue(end - start);
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
        SavedState data = state.getParcelable("SESSION_EDIT_MODEL_SAVED_STATE");
        Objects.requireNonNull(data).restore(this);
    }

    public void saveState(Bundle outState) {
        outState.putParcelable("SESSION_EDIT_MODEL_SAVED_STATE", new SavedState(this));
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
        private Session mSession;

        SavedState(EditSessionModel data) {
            sessionId = data.mSessionId;
            startTime = data.mStartTimeData.getValue() != null ? data.mStartTimeData.getValue() : -1;
            endTime = data.mEndTimeData.getValue() != null ? data.mEndTimeData.getValue() : -1;
            if (data.mIsRunningData.getValue() != null)
                isRunning = (byte) (data.mIsRunningData.getValue() ? 1 : 0);
            else
                isRunning = 3;

            mSession = data.mSession;
        }

        private SavedState(Parcel in) {
            sessionId = in.readLong();
            startTime = in.readLong();
            endTime = in.readLong();
            isRunning = in.readByte();

            boolean wasSession = in.readByte() == 1;
            if (wasSession) {
                long id = in.readLong();
                long start = in.readLong();
                long end = in.readLong();
                mSession = new Session(id, start, end);
            }
        }

        void restore(EditSessionModel dest) {
            dest.mStartTimeData.setValue(startTime > 0 ? startTime : null);
            dest.mEndTimeData.setValue(endTime > 0 ? endTime : null);
            if (isRunning == 3)
                dest.mIsRunningData.setValue(null);
            else
                dest.mIsRunningData.setValue(isRunning == 1);
            dest.mSession = mSession;
            dest.mIsChangedData.setValue(dest.calculatedIsChanged());
            dest.setId(sessionId);
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
            dest.writeByte((byte) (mSession != null ? 1 : 0));
            if (mSession != null) {
                dest.writeLong(mSession.getId());
                dest.writeLong(mSession.getStartTime());
                dest.writeLong(mSession.getEndTime());
            }
        }
    }
}