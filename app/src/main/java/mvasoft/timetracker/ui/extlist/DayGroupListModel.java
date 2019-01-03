package mvasoft.timetracker.ui.extlist;

import android.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import dagger.Lazy;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.processors.BehaviorProcessor;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayGroup;

public class DayGroupListModel {

    private final Lazy<AppPreferences> mPreferences;
    private final Lazy<DataRepository> mRepository;

    private final BehaviorProcessor<List<DayGroup>> mGroups;
    private final BehaviorProcessor<List<Long>> mDays;
    private final Flowable<Boolean> mHasRunningSessionFlowable;
    private final BehaviorProcessor<Long> mSummaryTimeFlowable;
    private final Flowable<Long> mTargetTimeFlowable;
    private final Flowable<Long> mTargetTimeDiffFlowable;
    private final CompositeDisposable mDisposable;


    public DayGroupListModel(Lazy<AppPreferences> preferences, Lazy<DataRepository> repository) {
        mPreferences = preferences;
        mRepository = repository;

        mGroups = BehaviorProcessor.createDefault(Collections.emptyList());
        mDays = BehaviorProcessor.createDefault(Collections.singletonList(0L));
        mSummaryTimeFlowable = BehaviorProcessor.createDefault(0L);
        mDisposable = new CompositeDisposable();
        ConnectableFlowable<List<DayGroup>> connectable = mDays
                .skip(1)
                .switchMap(mRepository.get()::getDayGroupsRx)
                .distinctUntilChanged()
                .replay(1);

        mDisposable.add(connectable.subscribe(
                groups -> mSummaryTimeFlowable.onNext(calculateSummary(groups))));
        connectable.subscribe(mGroups);

        mHasRunningSessionFlowable = mGroups.map(this::calculateHasRunningSession);
        mTargetTimeFlowable = mGroups.map(this::calculateTargetTime);
        mTargetTimeDiffFlowable = Flowable
                .combineLatest(mSummaryTimeFlowable, mTargetTimeFlowable, Pair::new)
                .map(pair -> (pair.first - pair.second));

        mDisposable.add(connectable.connect());
    }

    private boolean calculateHasRunningSession(List<DayGroup> groups) {
        if (groups != null)
            for (DayGroup item : groups)
                if (item.isRunning())
                    return true;
        return false;
    }

    public Flowable<List<DayGroup>> getItems() {
        return mGroups;
    }

    public Flowable<Boolean> hasRunningSessionsData() {
        return mHasRunningSessionFlowable;
    }

    public Flowable<Long> getSummaryTimeData() {
        return mSummaryTimeFlowable;
    }

    public Flowable<Long> getTargetTimeDiffData() {
        return mTargetTimeDiffFlowable;
    }

    public void invalidateValues() {
//         also update targetTimeDiff by notification from mSummaryTimeData if needed
        mSummaryTimeFlowable.onNext(calculateSummary(mGroups.getValue()));
    }

    public void setDateRange(long minDate, long maxDate) {
        List<Long> newDays = DateTimeHelper.daysList(minDate, maxDate);
        List<Long> oldDays = mDays.getValue();
        boolean isEqual = (newDays != null) && newDays.equals(oldDays);
        if (!isEqual && newDays != null)
            mDays.onNext(newDays);
    }

    public boolean isSingleDay() {
        return mDays.getValue().size() == 1;
    }

    private long calculateTargetTime(List<DayGroup> groups) {
        long res = 0;
        final HashSet<Long> remainingDays = new HashSet<>(mDays.getValue());
        if (groups != null) {
            for (DayGroup group : groups) {
                res += group.getTargetTime(mPreferences.get());
                remainingDays.remove(group.getDay());
            }
        }
        for (Long day : remainingDays)
            if (mPreferences.get().isWorkingDay(DateTimeHelper.dayOfWeek(day)))
                res += mPreferences.get().getTargetTimeInMin() * 60;
        return res;
    }

    private long calculateSummary(List<DayGroup> groups) {
        long res = 0;
        if (groups != null)
            for (DayGroup group : groups)
                res += group.getDuration();
        return res;
    }

    public void dispose() {
        if (mDisposable != null)
            mDisposable.dispose();
    }

}
