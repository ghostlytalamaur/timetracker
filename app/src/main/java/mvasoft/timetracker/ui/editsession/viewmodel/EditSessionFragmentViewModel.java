package mvasoft.timetracker.ui.editsession.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.editsession.model.SessionEditModel;
import mvasoft.timetracker.utils.DateTimeFormatters;


public class EditSessionFragmentViewModel extends BaseViewModel {

    private final DateTimeFormatters mFormatter;
    private final SessionEditModel mData;
    private final LiveData<String> mStartTimeData;
    private final LiveData<String> mEndTimeData;
    private final LiveData<String> mDurationData;

    @Inject
    EditSessionFragmentViewModel(@NonNull Application application, DataRepository repository) {
        super(application);

        mFormatter = new DateTimeFormatters();
        mData = new SessionEditModel(repository);
        mStartTimeData = Transformations.map(mData.getStartData(), start -> {
            if (start != null)
                return mFormatter.formatDate(start) + " " + mFormatter.formatTime(start);
            else
                return "";
        });

        mEndTimeData = Transformations.map(mData.getEndData(), end -> {
            if (end != null)
                return "id = " + mData.getSessionId() + " " + mFormatter.formatDate(end) + " " + mFormatter.formatTime(end);
            else
                return "id = " + mData.getSessionId();
        });

        mDurationData = Transformations.map(mData.getDurationData(), duration -> {
            if (duration != null)
                return mFormatter.formatDuration(duration);
            else
                return "";
        });
    }

    public SessionEditModel getModel() {
        return mData;
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

}
