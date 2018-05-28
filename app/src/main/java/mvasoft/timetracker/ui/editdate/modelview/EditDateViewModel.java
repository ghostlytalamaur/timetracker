package mvasoft.timetracker.ui.editdate.modelview;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.editdate.model.EditDateModel;
import mvasoft.timetracker.utils.DateTimeFormatters;

public class EditDateViewModel extends BaseViewModel {

    private final DataRepository mRepository;
    private final AppPreferences mPreferences;
    private final DateTimeFormatters mFormatter;
    private final EditDateModel mModel;
    private final LiveData<String> mTargetTimeData;
    private final LiveData<Boolean> mIsWorkingDayData;
    private final LiveData<String> mIdData;
    private final LiveData<Boolean> mIsChangedData;

    @Inject
    EditDateViewModel(@NonNull Application application, DataRepository repository,
                      AppPreferences preferences) {
        super(application);
        mRepository = repository;
        mPreferences = preferences;
        mFormatter = new DateTimeFormatters();
        mModel = new EditDateModel(mRepository, mPreferences);
        mTargetTimeData = LiveDataReactiveStreams.fromPublisher(
                mModel.getTargetMin()
                        .map(minutes -> mFormatter.formatDuration(minutes * 60))
                        .toFlowable(BackpressureStrategy.LATEST)
        );

        mIsWorkingDayData = LiveDataReactiveStreams.fromPublisher(
                mModel.getIsWorkingDay().toFlowable(BackpressureStrategy.LATEST)
        );

        mIdData = LiveDataReactiveStreams.fromPublisher(
                mModel.getId()
                        .map(String::valueOf)
                        .toFlowable(BackpressureStrategy.LATEST)
        );

        mIsChangedData = LiveDataReactiveStreams.fromPublisher(
                mModel.getIsChangedObservable()
                        .toFlowable(BackpressureStrategy.LATEST)
        );
    }

    public LiveData<String> getTargetTimeData() {
        return mTargetTimeData;
    }

    public LiveData<Boolean> getIsWorkingDay() {
        return mIsWorkingDayData;
    }

    public LiveData<String> getDayId() {
        return mIdData;
    }

    public void setDate(long date) {
        mModel.setDate(date);
    }

    public void save() {
        mModel.save();
    }

    public EditDateModel getModel() {
        return mModel;
    }

    public LiveData<Boolean> getIsChanged() {
        return mIsChangedData;
    }
}
