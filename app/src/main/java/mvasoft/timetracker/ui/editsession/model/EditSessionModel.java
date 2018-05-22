package mvasoft.timetracker.ui.editsession.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.Session;

// TODO: 04.05.2018 recator this class
public class EditSessionModel {

    private final DataRepository mRepository;
    private long mSessionId;
    private LiveData<Session> mSessionLiveData;
    private MediatorLiveData<Long> mStartTimeData;
    private MediatorLiveData<Long> mEndTimeData;
    private MediatorLiveData<Long> mDurationData;
    private LoggedLiveData<Boolean> mIsRunningData;
    private MediatorLiveData<Boolean> mIsChangedData;

    public EditSessionModel(DataRepository repository) {
        super();
        mRepository = repository;

        mStartTimeData = new MediatorLiveData<>();
        mEndTimeData = new MediatorLiveData<>();
        mDurationData = new MediatorLiveData<>();
        mIsRunningData = new LoggedLiveData<>();
        mIsChangedData = new MediatorLiveData<>();

        mDurationData.addSource(mStartTimeData, start -> updateDuration());
        mDurationData.addSource(mEndTimeData, end -> updateDuration());

//        mEndTimeData.addSource(mIsRunningData, bIsRunning -> {
//            if (bIsRunning == null)
//                return;
//
//            Long cur = mEndTimeData.getValue();
//            if (bIsRunning) {
//                if (cur != null && cur == mIsRunningData.mLastChangedAtUnixTime)
//                    mEndTimeData.setValue(null);
//                return;
//            }
//            if (cur == null || cur == 0)
//                mEndTimeData.setValue(mIsRunningData.mLastChangedAtUnixTime);
//        });

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
            return false;

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

    public void setStartTime(int hourOfDay, int minute) {
        // TODO: thinking: set values using current date?
        if (mStartTimeData.getValue() == null)
            return;

        setStartTime(DateTimeHelper.withTime(mStartTimeData.getValue(), hourOfDay, minute));
    }

    public void setEndTime(long end) {
        if (mEndTimeData.getValue() != null && mEndTimeData.getValue() == end)
            return;

        mEndTimeData.setValue(end);
    }

    public void setEndTime(int hourOfDay, int minute) {
        if (mEndTimeData.getValue() == null)
            return;

        setEndTime(DateTimeHelper.withTime(mEndTimeData.getValue(), hourOfDay, minute));
    }

    public void setStartDate(int year, int month, int dayOfMonth) {
        if (mStartTimeData.getValue() == null)
            return;

        setEndTime(DateTimeHelper.withDate(mStartTimeData.getValue(), year, month, dayOfMonth));
    }

    public void setEndDate(int year, int month, int dayOfMonth) {
        if (mEndTimeData.getValue() == null)
            return;

        setEndTime(DateTimeHelper.withDate(mEndTimeData.getValue(), year, month, dayOfMonth));
    }

    private boolean isChanged() {
        return mIsChangedData.getValue() != null && mIsChangedData.getValue();
    }


    public LiveData<Session> getSession() {
        return mSessionLiveData;
    }

    public boolean isRunning() {
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

    public void updateDuration() {
        if (mStartTimeData.getValue() == null)
            return;

        long start = mStartTimeData.getValue();
        long end = 0;
        if (isRunning() || mEndTimeData.getValue() == null)
            end = System.currentTimeMillis() / 1000;
        else
            end = mEndTimeData.getValue();

        mDurationData.postValue(end - start);
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

        SavedState(EditSessionModel data) {
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

        void restore(EditSessionModel dest) {
            dest.mStartTimeData.setValue(startTime > 0 ? startTime : null);
            dest.mEndTimeData.setValue(endTime > 0 ? endTime : null);
            if (isRunning == 3)
                dest.mIsRunningData.setValue(null);
            else
                dest.mIsRunningData.setValue(isRunning == 1);
            dest.mIsChangedData.setValue(false);
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
        }
    }


    private static class LoggedLiveData<T> extends MediatorLiveData<T> {

        public long mLastChangedAtUnixTime = -1;

        @Override
        public void setValue(T value) {
            mLastChangedAtUnixTime = System.currentTimeMillis() / 1000;
            super.setValue(value);
        }
    }

}