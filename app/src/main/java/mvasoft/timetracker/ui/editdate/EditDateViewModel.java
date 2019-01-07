package mvasoft.timetracker.ui.editdate;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.annotation.NonNull;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;
import timber.log.Timber;

public class EditDateViewModel extends BaseViewModel {

    private final DataRepository mRepository;
    private final AppPreferences mPreferences;
    private final DateTimeFormatters mFormatter;
    private final LiveData<String> mTargetTimeData;
    private final LiveData<Boolean> mIsWorkingDayData;
    private final LiveData<Boolean> mIsChangedData;
    private final LiveData<String> mDateData;
    private Disposable mDisposable;
    private DayDescription mOriginalDayDescription;
    private final MutableLiveData<DayDescription> mDayDescriptionData;

    @Inject
    EditDateViewModel(@NonNull Application application, DataRepository repository, AppPreferences preferences) {
        super(application);

        mRepository = repository;
        mPreferences = preferences;

        mDayDescriptionData = new MutableLiveData<>();
        final long today = System.currentTimeMillis() / 1000;
        mDayDescriptionData.setValue(new DayDescription(0,
                today, mPreferences.getTargetTimeInMin(),
                mPreferences.isWorkingDay(DateTimeHelper.dayOfWeek(today))));

        mFormatter = new DateTimeFormatters();

        mDateData = Transformations.map(mDayDescriptionData, dd ->
                mFormatter.formatDate(dd.getDate()));
        mTargetTimeData = Transformations.map(mDayDescriptionData, dd ->
                mFormatter.formatDuration(dd.getTargetDuration() * 60 ));
        mIsWorkingDayData = Transformations.map(mDayDescriptionData, DayDescription::isWorkingDay);
        mIsChangedData = Transformations.map(mDayDescriptionData, dd ->
                !dd.equals(mOriginalDayDescription));
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

    DayDescription getDayDescription() {
        assert mDayDescriptionData.getValue() != null;
        return mDayDescriptionData.getValue();
    }

    void setTargetTime(long timeInMin) {
        DayDescription dd = mDayDescriptionData.getValue();
        assert dd != null;
        mDayDescriptionData.setValue(
                new DayDescription(dd.getId(), dd.getDate(), timeInMin, dd.isWorkingDay()));
    }


    void setIsWorkingDay(boolean isWorkingDay) {
        DayDescription dd = mDayDescriptionData.getValue();
        assert dd != null;
        mDayDescriptionData.setValue(
                new DayDescription(dd.getId(), dd.getDate(), dd.getTargetDuration(), isWorkingDay));
    }

    public void setDate(long date) {
        if (date == getDayDescription().getDate())
            return;

        if (mDisposable != null)
            mDisposable.dispose();

        mDayDescriptionData.setValue(
                new DayDescription(0, date,
                        mPreferences.getTargetTimeInMin(),
                        mPreferences.isWorkingDay(DateTimeHelper.dayOfWeek(date))));
        Flowable<DayDescription> dd = mRepository.getDayDescriptionRx(date);
        mDisposable = dd.subscribe(this::setOriginalDayDescription);
    }

    private void setOriginalDayDescription(DayDescription dayDescription) {
        Timber.d("New data received.");
        mOriginalDayDescription = dayDescription;
        mDayDescriptionData.postValue(dayDescription);
    }

    void save() {
        mRepository.updateDayDescription(mDayDescriptionData.getValue());
    }

    LiveData<Boolean> getIsChanged() {
        return mIsChangedData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
    }
}
