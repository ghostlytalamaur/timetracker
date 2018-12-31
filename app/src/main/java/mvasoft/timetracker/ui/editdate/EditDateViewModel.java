package mvasoft.timetracker.ui.editdate;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeFormatters;

public class EditDateViewModel extends BaseViewModel {

    private final DateTimeFormatters mFormatter;
    private final EditDateModel mModel;
    private final LiveData<String> mTargetTimeData;
    private final LiveData<Boolean> mIsWorkingDayData;
    private final LiveData<String> mIdData;
    private final LiveData<Boolean> mIsChangedData;
    private final LiveData<String> mDateData;

    @Inject
    EditDateViewModel(@NonNull Application application, EditDateModel model) {
        super(application);

        mModel = model;
        mFormatter = new DateTimeFormatters();

        mDateData = LiveDataReactiveStreams.fromPublisher(
                mModel.getDate()
                        .map(mFormatter::formatDate)
        );

        mTargetTimeData = LiveDataReactiveStreams.fromPublisher(
                mModel.getTargetMin()
                        .map(minutes -> mFormatter.formatDuration(minutes * 60))
        );

        mIsWorkingDayData = LiveDataReactiveStreams.fromPublisher(
                mModel.getIsWorkingDay()
        );

        mIdData = LiveDataReactiveStreams.fromPublisher(
                mModel.getId()
                        .map(String::valueOf)
        );

        mIsChangedData = LiveDataReactiveStreams.fromPublisher(
                mModel.getIsChangedObservable()
        );
    }

    public LiveData<String> getDate() {
        return mDateData;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        mModel.clear();
    }
}
