package mvasoft.timetracker.ui.extlist.model;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import java.util.HashSet;
import java.util.List;

import dagger.Lazy;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayGroup;

public class DayGroupListModel {

    private final Lazy<AppPreferences> mPreferences;
    private final Lazy<DataRepository> mRepository;
    private final LiveData<List<DayGroup>> mGroups;
    private final MutableLiveData<List<Long>> mDays;

    public DayGroupListModel(Lazy<AppPreferences> preferences, Lazy<DataRepository> repository) {
        mPreferences = preferences;
        mRepository = repository;
        mDays = new MutableLiveData<>();
        mGroups = Transformations.switchMap(mDays, new Function<List<Long>, LiveData<List<DayGroup>>>() {
            @Override
            public LiveData<List<DayGroup>> apply(List<Long> days) {
                if (days == null || days.size() == 0)
                    return null;

                return mRepository.get().getDayGroups(days);
            }
        });
    }

    public LiveData<List<DayGroup>> getItems() {
        return mGroups;
    }

    public boolean hasRunningSessions() {
        if (mGroups.getValue() != null)
            for (DayGroup group : mGroups.getValue())
                if (group.hasRunningSessions())
                    return true;
        return false;
    }

    private long getTargetTime() {
        long res = 0;
        if (mDays.getValue() == null)
            return res;

        final HashSet<Long> remainingDays = new HashSet<>(mDays.getValue());
        if (mGroups.getValue() != null) {
            for (DayGroup group : mGroups.getValue()) {
                res += group.getTargetTime(mPreferences.get());
                remainingDays.remove(group.getDay());
            }
        }
        for (Long day : remainingDays)
            if (mPreferences.get().isWorkingDay(DateTimeHelper.dayOfWeek(day)))
                res += mPreferences.get().getTargetTimeInMin() * 60;
        return res;
    }

    public long getSummaryTime() {
        long res = 0;
        if (mGroups.getValue() != null)
            for (DayGroup group : mGroups.getValue())
                res += group.getDuration();
        return res;
    }

    public long getTargetTimeDiff() {
        return getSummaryTime() - getTargetTime();
    }

    public void setDateRange(long minDate, long maxDate) {
        List<Long> newDays = DateTimeHelper.daysList(minDate, maxDate);
        List<Long> oldDays = mDays.getValue();
        boolean isEqual = (newDays != null) && newDays.equals(oldDays);
        if (!isEqual)
            mDays.setValue(newDays);
    }

    public boolean isSingleDay() {
        return mDays.getValue() != null && mDays.getValue().size() == 1;
    }
}
