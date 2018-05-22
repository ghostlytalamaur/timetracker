package mvasoft.timetracker.ui.editsession.viewmodel;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.editsession.model.EditSessionModel;
import mvasoft.timetracker.utils.DateTimeFormatters;


public class EditSessionFragmentViewModel extends BaseViewModel {

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
    EditSessionFragmentViewModel(@NonNull Application application, DataRepository repository) {
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

        mEndTimeData = Transformations.map(mData.getEndData(), end -> {
            if (end != null)
                return mFormatter.formatTime(end);
            else
                return "";
        });

        mDurationData = Transformations.map(mData.getDurationData(), duration -> {
            if (duration != null)
                return mFormatter.formatDuration(duration);
            else
                return "";
        });

        mStartDateData = Transformations.map(mData.getStartData(), start -> {
            if (start != null)
                return mFormatter.formatDate(start);
            else
                return "";
        });

        mEndDateData = Transformations.map(mData.getEndData(), end -> {
            if (end != null)
                return mFormatter.formatDate(end);
            else
                return "";
        });

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

    public MutableLiveData<Boolean> getIsRunning() {
        return mData.getIsRunningData();
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
    public void resume() {
        mData.getIsChangedData().observeForever(mIsChangedObserver);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void pause() {
        mData.getIsChangedData().removeObserver(mIsChangedObserver);
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
            mUpdateFuture = null;
        }
    }

}
