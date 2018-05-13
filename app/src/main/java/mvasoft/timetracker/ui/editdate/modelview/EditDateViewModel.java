package mvasoft.timetracker.ui.editdate.modelview;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;

public class EditDateViewModel extends BaseViewModel {

    private final DataRepository mRepository;
    private final MutableLiveData<Long> mDateLiveData;
    private final LiveData<DayDescription> mDayDescriptionLiveData;
    private final MediatorLiveData<Long> mIdLiveData;
    private final MediatorLiveData<Boolean> mIsWorkingDayLiveData;
    private final MediatorLiveData<Long> mTargetTimeLiveData;
    @Inject
    AppPreferences mPreferences;

    @Inject
    EditDateViewModel(@NonNull Application application, DataRepository repository) {
        super(application);
        mDateLiveData = new MutableLiveData<>();
        mRepository = repository;

        mDayDescriptionLiveData = Transformations.switchMap(mDateLiveData, new Function<Long, LiveData<DayDescription>>() {
            @Override
            public LiveData<DayDescription> apply(Long input) {
                return mRepository.getDayDescription(input);
            }
        });

        mIsWorkingDayLiveData = new MediatorLiveData<>();
        mIsWorkingDayLiveData.addSource(mDayDescriptionLiveData, (dayDescription) -> {
            if (dayDescription != null)
                mIsWorkingDayLiveData.setValue(dayDescription.isWorkingDay());
        });

        mTargetTimeLiveData = new MediatorLiveData<>();
        mTargetTimeLiveData.addSource(mDayDescriptionLiveData, (dayDescription) -> {
            if (dayDescription != null)
                mTargetTimeLiveData.setValue(dayDescription.getTargetDuration());
        });

        mIdLiveData = new MediatorLiveData<>();
        mIdLiveData.addSource(mDayDescriptionLiveData, (dayDescription) -> {
            if (dayDescription != null)
                mIdLiveData.setValue(dayDescription.getId());
        });
    }

    @Bindable
    public MutableLiveData<Long> getTargetTime() {
        return mTargetTimeLiveData;
    }

    @Bindable
    public MutableLiveData<Boolean> getIsWorkingDay() {
        return mIsWorkingDayLiveData;
    }

    public LiveData<Long> getDayId() {
        return mIdLiveData;
    }

    public void setDate(long date) {
        if (mDateLiveData.getValue() != null && mDateLiveData.getValue() == date)
            return;

        Boolean isWorking = mPreferences.isWorkingDay(DateTimeHelper.dayOfWeek(date));
        mIsWorkingDayLiveData.setValue(isWorking);
        mTargetTimeLiveData.setValue(isWorking ? mPreferences.getTargetTimeInMin() : 0);
        mDateLiveData.setValue(date);
    }

    public void save() {
        if (mDateLiveData.getValue() == null ||
                getTargetTime().getValue() == null || getIsWorkingDay().getValue() == null)
            return;

        long id = getDayId().getValue() != null ? getDayId().getValue() : 0;
        long date = mDateLiveData.getValue();
        boolean isWorkingDay = getIsWorkingDay().getValue();
        long target = isWorkingDay ? getTargetTime().getValue() : 0;

        DayDescription d = new DayDescription(id , date, target, isWorkingDay);
        mRepository.updateDayDescription(d);
    }
}
