package mvasoft.timetracker.ui.extlist.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
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
    private final LiveData<Boolean> mHasRunningSession;
    private final MediatorLiveData<Long> mSummaryTimeData;
    private final MediatorLiveData<Long> mTargetTimeDiffData;
    private boolean mTargetTimeValid = false;
    private long mTargetTime;


    public DayGroupListModel(Lazy<AppPreferences> preferences, Lazy<DataRepository> repository) {
        mPreferences = preferences;
        mRepository = repository;
        mDays = new MutableLiveData<>();

        LiveData<List<DayGroup>> data = Transformations.switchMap(mDays, days -> {
            if (days == null || days.size() == 0)
                return null;

            return mRepository.get().getDayGroups(days);
        });

        mGroups = Transformations.map(data, days -> {
            invalidateTargetTime();
            return days;
        });

        mHasRunningSession = Transformations.map(mGroups, list -> {
            if (list != null)
                for (DayGroup item : list)
                    if (item.isRunning())
                        return true;
            return false;
        });

        mSummaryTimeData = new MediatorLiveData<>();
        mSummaryTimeData.addSource(mGroups, groups ->
                mSummaryTimeData.setValue(calculateSummary()));
        mSummaryTimeData.setValue((long) 0);

        mTargetTimeDiffData = new MediatorLiveData<>();
        mTargetTimeDiffData.addSource(mSummaryTimeData, summary ->
                mTargetTimeDiffData.setValue(calculateTargetTimeDiff()));
    }

    public LiveData<List<DayGroup>> getItems() {
        return mGroups;
    }

    public LiveData<Boolean> hasRunningSessionsData() {
        return mHasRunningSession;
    }

    public LiveData<Long> getSummaryTimeData() {
        return mSummaryTimeData;
    }

    public LiveData<Long> getTargetTimeDiffData() {
        return mTargetTimeDiffData;
    }

    public void invalidateValues() {
        // also update targetTimeDiff by notification from mSummaryTimeData if needed
        mSummaryTimeData.postValue(calculateSummary());
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

    void invalidateTargetTime() {
        mTargetTimeValid = false;
    }

    private long calculateTargetTime() {
        if (!mTargetTimeValid) {
            long res = 0;
            if (mDays.getValue() == null)
                return res;

            final HashSet<Long> remainingDays = new HashSet<>(mDays.getValue());
            List<DayGroup> groups = mGroups.getValue();
            if (groups != null) {
                for (DayGroup group : groups) {
                    res += group.getTargetTime(mPreferences.get());
                    remainingDays.remove(group.getDay());
                }
            }
            for (Long day : remainingDays)
                if (mPreferences.get().isWorkingDay(DateTimeHelper.dayOfWeek(day)))
                    res += mPreferences.get().getTargetTimeInMin() * 60;
            mTargetTime = res;
            mTargetTimeValid = true;
        }
        return mTargetTime;
    }

    private long calculateSummary() {
        long res = 0;
        List<DayGroup> groups = mGroups.getValue();
        if (groups != null)
            for (DayGroup group : groups)
                res += group.getDuration();
        return res;
    }

    private long calculateTargetTimeDiff() {
        long summary = mSummaryTimeData.getValue() != null ? mSummaryTimeData.getValue() : 0;
        return summary - calculateTargetTime();
//        return summary - calculateTargetTime();
    }

//    public static class WeakObservableLiveData<T> extends MediatorLiveData<T> {
//        private List<Observer<T>> mWeakObservers;
//
//        public static <X, Y> WeakObservableLiveData<Y> fromLiveData(LiveData<X> trigger,
//                                                   final Function<X, LiveData<Y>> func) {
//            final WeakObservableLiveData<Y> result = new WeakObservableLiveData<>();
//            result.addSource(trigger, new Observer<X>() {
//                LiveData<Y> mSource;
//
//                @Override
//                public void onChanged(@Nullable X x) {
//                    LiveData<Y> newLiveData = func.apply(x);
//                    if (mSource == newLiveData) {
//                        return;
//                    }
//                    if (mSource != null) {
//                        result.removeSource(mSource);
//                    }
//                    mSource = newLiveData;
//                    if (mSource != null) {
//                        result.addSource(mSource, new Observer<Y>() {
//                            @Override
//                            public void onChanged(@Nullable Y y) {
//                                result.setValue(y);
//                            }
//                        });
//                    }
//                }
//            });
//            return result;
//        }
//
//        public void addWeakObserver(Observer<T> observer) {
//            mWeakObservers.add(observer);
//        }
//
//        public void removeWeakObserver(Observer<T> observer){
//            mWeakObservers.remove(observer);
//        }
//
//        @Override
//        protected void setValue(T value) {
//            super.setValue(value);
//            if (hasActiveObservers())
//                for (Observer<T> observer : mWeakObservers)
//                    observer.onChanged(value);
//        }
//    }
}
