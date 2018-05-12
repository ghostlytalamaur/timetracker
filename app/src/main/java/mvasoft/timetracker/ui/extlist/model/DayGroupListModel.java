package mvasoft.timetracker.ui.extlist.model;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;

import dagger.Lazy;
import mvasoft.timetracker.core.CalculatedValue;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayGroup;

public class DayGroupListModel {

    private final Lazy<AppPreferences> mPreferences;
    private final Lazy<DataRepository> mRepository;
    private final LiveData<List<DayGroup>> mGroups;
    private final MutableLiveData<List<Long>> mDays;
    private final CalculatedValue<Boolean> mHasRunningSession;
    private final CalculatedValue<Long> mTargetTime;

    public DayGroupListModel(Lazy<AppPreferences> preferences, Lazy<DataRepository> repository) {
        mPreferences = preferences;
        mRepository = repository;
        mDays = new MutableLiveData<>();

        mGroups= Transformations.switchMap(mDays, new Function<List<Long>, LiveData<List<DayGroup>>>() {
            @Override
            public LiveData<List<DayGroup>> apply(List<Long> days) {
                if (days == null || days.size() == 0)
                    return null;

                return mRepository.get().getDayGroups(days);
            }
        });

        mGroups.observeForever(new GroupsObserver(this));

        mHasRunningSession = new CalculatedValue<>(new CalculatedValue.ValueCalculator<Boolean>() {
            @Override
            public Boolean calculate() {
                if (mGroups.getValue() != null)
                    for (DayGroup item : mGroups.getValue())
                        if (item.isRunning())
                            return true;
                return false;
            }
        });

        mTargetTime = new CalculatedValue<>(new CalculatedValue.ValueCalculator<Long>() {
            @Override
            public Long calculate() {
                return calculateTargetTime();
            }
        });
    }

    public LiveData<List<DayGroup>> getItems() {
        return mGroups;
    }

    public boolean hasRunningSessions() {
        return mHasRunningSession.getValue() != null && mHasRunningSession.getValue();
    }

    private long calculateTargetTime() {
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

    private long getTargetTime() {
        return mTargetTime.getValue() != null ? mTargetTime.getValue() : 0;
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


    private void invalidate() {
        mHasRunningSession.invalidate();
        mTargetTime.invalidate();
    }

    private static class GroupsObserver implements Observer<List<DayGroup>> {

        private final WeakReference<DayGroupListModel> mModel;

        GroupsObserver(DayGroupListModel model) {
            mModel = new WeakReference<>(model);
        }

        @Override
        public void onChanged(@Nullable List<DayGroup> list) {
            DayGroupListModel model = mModel.get();
            if (model != null)
                model.invalidate();
        }
    }

}
