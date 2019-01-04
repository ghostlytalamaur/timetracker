package mvasoft.timetracker.ui.editsession;

import android.app.Application;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.Transformations;
import androidx.annotation.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeFormatters;


public class EditSessionViewModel extends BaseViewModel {

    private final DateTimeFormatters mFormatter;
    private final EditSessionModel mData;
    private final LiveData<String> mStartDateData;
    private final LiveData<String> mStartTimeData;
    private final LiveData<String> mEndDateData;
    private final LiveData<String> mEndTimeData;
    private final LiveData<String> mDurationData;
    private final Observer<Boolean> mIsChangedObserver;
    private final ScheduledExecutorService mUpdateExecutor;
    private ScheduledFuture<?> mUpdateFuture;
    @Inject
    EditSessionViewModel(@NonNull Application application, DataRepository repository) {
        super(application);

        mUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        mFormatter = new DateTimeFormatters();
        mData = new EditSessionModel(repository);
        mStartTimeData = Transformations.map(mData.getStartData(), start -> {
            if (start != null)
                return mFormatter.formatTime(start);
            else
                return "";
        });

        mEndTimeData = Transformations.map(mData.getEndData(), end ->
                end != null ? mFormatter.formatTime(end) : "");

        mDurationData = Transformations.map(mData.getDurationData(), duration ->
                duration != null ?  mFormatter.formatDuration(duration) : "");

        mStartDateData = Transformations.map(mData.getStartData(), start ->
                start != null ? mFormatter.formatDate(start) : "");

        mEndDateData = Transformations.map(mData.getEndData(), end ->
                end != null ? mFormatter.formatDate(end) : "");

        mIsChangedObserver = isChanged -> updateTimer();
    }

    public EditSessionModel getModel() {
        return mData;
    }

    public LiveData<String> getStartDate() {
        return mStartDateData;
    }

    public LiveData<String> getEndDate() {
        return mEndDateData;
    }

    public LiveData<String> getStartTime() {
        return mStartTimeData;
    }

    public LiveData<String> getEndTime() {
        return mEndTimeData;
    }

    public LiveData<String> getDuration() {
        return mDurationData;
    }

    public LiveData<Boolean> getIsChanged() {
        return mData.getIsChangedData();
    }

    public LiveData<Boolean> getIsRunning() {
        return mData.getIsRunningData();
    }

    public void setIsRunning(boolean isRunning) {
        mData.setIsRunning(isRunning);
    }

    public void saveSession() {
        mData.safeSession();
    }

    private void updateTimer() {
        boolean isRunning = mData.isRunning();
        if (!isRunning) {
            if (mUpdateFuture != null)
                mUpdateFuture.cancel(true);
            mUpdateFuture = null;
            mData.updateDuration();
        }
        else if (mUpdateFuture == null) {
            mUpdateFuture = mUpdateExecutor.scheduleWithFixedDelay(mData::updateDuration,
                    0, 1, TimeUnit.SECONDS);

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        mData.getIsChangedData().observeForever(mIsChangedObserver);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        mData.getIsChangedData().removeObserver(mIsChangedObserver);
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
            mUpdateFuture = null;
        }
    }

}
