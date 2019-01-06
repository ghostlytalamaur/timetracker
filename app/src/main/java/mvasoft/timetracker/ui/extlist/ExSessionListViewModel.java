package mvasoft.timetracker.ui.extlist;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import dagger.Lazy;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import mvasoft.recyclerbinding.ItemViewModel;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;
import mvasoft.timetracker.vo.SessionsGroup;
import mvasoft.utils.CollectionsUtils;
import timber.log.Timber;


public class ExSessionListViewModel extends BaseViewModel {

    private final DateTimeFormatters mFormatter;
    private final Lazy<AppPreferences> mAppPreferences;
    private final Lazy<DataRepository> mRepository;

    private List<SessionsGroup> mGroups;
    private List<DayDescription> mDayDescriptions;

    private long mSummaryTime;
    private long mTargetTime;

    private final MutableLiveData<List<ItemViewModel>> mListModel;
    private final ScheduledExecutorService mUpdateExecutor;
    private final MutableLiveData<String> mSummaryTimeLiveData;
    private final MutableLiveData<String> mTargetDiffStrLiveData;
    private final MutableLiveData<Boolean> mIsTargetAchieved;
    private final CompositeDisposable mDisposable;
    private ScheduledFuture<?> mUpdateFuture;
    private LiveData<Boolean> mOpenedSessionId;
    private final BehaviorProcessor<DateRange> mDateRange;
    private final BehaviorProcessor<SessionsGroup.GroupType> mGroupType;


    @Inject
    ExSessionListViewModel(@NonNull Application application, Lazy<DataRepository> repository,
                           Lazy<AppPreferences> appPreferences) {
        super(application);

        mAppPreferences = appPreferences;
        mRepository = repository;
        mListModel = new MutableLiveData<>();
        mFormatter = new DateTimeFormatters();
        mSummaryTimeLiveData = new MutableLiveData<>();
        mTargetDiffStrLiveData = new MutableLiveData<>();
        mIsTargetAchieved = new MutableLiveData<>();
        mUpdateExecutor = Executors.newSingleThreadScheduledExecutor();

        mGroupType = BehaviorProcessor.createDefault(SessionsGroup.GroupType.gtNone);
        long today = System.currentTimeMillis() / 1000;
        mDateRange = BehaviorProcessor.createDefault(new DateRange(today, today));

        Flowable<List<Session>> sessions = mDateRange
                .flatMap(range -> mRepository.get().getSessionsRx(range.start, range.end))
                .doOnNext(list -> Timber.d("New sessions received"));
        Flowable<List<SessionsGroup>> sessionsGroups = Flowable.combineLatest(sessions, mGroupType, Pair::new)
                .map(pair -> groupSessions(pair.second, pair.first));

        Flowable<List<DayDescription>> dayDescriptionsFlowable = mDateRange
                .flatMap(range -> mRepository.get().getDayDescriptionsRx(range.start, range.end))
                .doOnNext(list -> Timber.d("New day descriptions received"));

        mDisposable = new CompositeDisposable();
        mDisposable.add(sessionsGroups.subscribe(this::setGroups));
        mDisposable.add(dayDescriptionsFlowable.subscribe(this::setDayDescriptions));

        new DefaultLifecycleObserver() {
            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                onStop(owner);
            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                onStart(owner);
            }
        };
    }

    void setGroupType(SessionsGroup.GroupType groupType) {
        mGroupType.onNext(groupType);
    }

    SessionsGroup.GroupType getGroupType() {
        return mGroupType.getValue();
    }

    private void setDayDescriptions(List<DayDescription> dayDescriptions) {
        mDayDescriptions = dayDescriptions;
        updateTargetTime();
    }

    private List<SessionsGroup> groupSessions(SessionsGroup.GroupType groupType, List<Session> sessions) {
        List<SessionsGroup> groups = new ArrayList<>();
        for (List<Session> list : CollectionsUtils.group(sessions, SessionsGroup.getGroupFunction(groupType))) {
            Collections.sort(list, (l, r) -> {
                int res = Long.compare(l.getStartTime(), r.getStartTime());
                if (res == 0)
                    res = Long.compare(l.getEndTime(), r.getEndTime());
                return res;
            });
            groups.add(new SessionsGroup(list));
        }

        Collections.sort(groups, (l, r) -> {
            int res = Long.compare(l.getStart(), r.getStart());
            if (res == 0)
                res = Long.compare(l.getEnd(), r.getEnd());
            return res;
        });
        return groups;
    }

    public LiveData<String> getSummaryTime() {
        return mSummaryTimeLiveData;
    }

    public LiveData<String> getTargetDiff() {
        return mTargetDiffStrLiveData;
    }

    public LiveData<Boolean> getIsTargetAchieved() {
        return mIsTargetAchieved;
    }

    LiveData<Boolean> getOpenedSessionId() {
        if (mOpenedSessionId == null)
            mOpenedSessionId = LiveDataReactiveStreams.fromPublisher(
                    mRepository.get().getOpenedSessionsIds().map(list -> list.size() > 0));

        return mOpenedSessionId;
    }

    LiveData<List<ItemViewModel>> getListModel() {
        return mListModel;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        updateTimer(mGroups != null && CollectionsUtils.contains(mGroups, SessionsGroup::hasOpenedSessions));
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true); // fix me: run on onStart
            mUpdateFuture = null;
        }
    }

    void toggleSession() {
        mRepository.get().toggleSession();
    }

    @Override
    protected void onCleared() {
        mDisposable.dispose();
        super.onCleared();
    }

    void deleteSelected(Set<Long> modelIds) {
        mRepository.get().deleteSessions(getSelectedSessionsIds(modelIds));
    }

    boolean copyToClipboard(Set<Long> modelIds) {
        final ClipboardManager clipboard = (ClipboardManager)
                getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return false;

        StringBuilder text = new StringBuilder();

        DateTimeFormatters formatter = new DateTimeFormatters(
                DateTimeFormatters.DateTimeFormattersType.dtft_Clipboard);

        List<ItemViewModel> list = mListModel.getValue();
        if (list == null)
            return false;

        for (ItemViewModel item : list) {
            if (item instanceof GroupItemViewModel && modelIds.contains(item.getId()))
                text.append(((GroupItemViewModel) item).getClipboardString(formatter));
        }

        String str = text.toString();
        if (!str.isEmpty()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Sessions", str));
            return true;
        }

        return false;
    }

    void setDate(long dateStart, long dateEnd) {
        mDateRange.onNext(new DateRange(dateStart, dateEnd));
    }

    private void updateTargetTime() {
        mTargetTime = 0;

        DateRange range = mDateRange.getValue();
        DateTime start = new DateTime(range.start).withTimeAtStartOfDay();
        DateTime end = new DateTime(range.end).withTime(23, 59, 59, 0);
        int daysCount = Days.daysBetween(start, end).getDays() + 1;
        final HashSet<DateTime> remainingDays = new HashSet<>(daysCount);
        for (int nday = 1; nday < daysCount; nday++) {
            remainingDays.add(start);
            start.plusDays(1);
        }

        if (mDayDescriptions != null) {
            for (DayDescription dayDescription : mDayDescriptions) {
                if (dayDescription.isWorkingDay()) {
                    mTargetTime += dayDescription.getTargetDuration() * 60;
                }
                remainingDays.remove(new DateTime(dayDescription.getDate()).withTimeAtStartOfDay());
            }
        }
        for (DateTime day : remainingDays)
            if (mAppPreferences.get().isWorkingDay(day.getDayOfWeek()))
                mTargetTime += mAppPreferences.get().getTargetTimeInMin() * 60;
        updateTargetDiff();
    }

    private void updateTargetDiff() {
        long targetDiff = mSummaryTime - mTargetTime;
        mTargetDiffStrLiveData.postValue(mFormatter.formatDuration(targetDiff));
        mIsTargetAchieved.postValue(targetDiff >= 0);
    }

    private void setGroups(List<SessionsGroup> groups) {
        mGroups = groups;

        mListModel.postValue(buildListItems(mGroups));
        updateSummary();
        updateTimer(CollectionsUtils.contains(groups, SessionsGroup::hasOpenedSessions));
    }

    private void updateSummary() {
        mSummaryTime = 0;
        if (mGroups != null)
            for (SessionsGroup group : mGroups) {
                mSummaryTime += group.calculateDuration();
            }
        mSummaryTimeLiveData.postValue(mFormatter.formatDuration(mSummaryTime));

        updateTargetDiff();
    }

    private void updateTimer(Boolean hasRunning) {
        if (hasRunning != null && !hasRunning) {
            if (mUpdateFuture != null)
                mUpdateFuture.cancel(true);
            mUpdateFuture = null;
            updateSummary();

        } else if (mUpdateFuture == null) {
            mUpdateFuture = mUpdateExecutor.scheduleWithFixedDelay(this::updateRunningItems,
                    0, 1, TimeUnit.SECONDS);
        }
    }

    private void updateRunningItems() {
        List<ItemViewModel> list = mListModel.getValue();
        if (list != null)
            for (ItemViewModel item : list) {
                if (item instanceof GroupItemViewModel && ((GroupItemViewModel) item).getIsRunning())
                    ((GroupItemViewModel) item).updateDuration();
            }

        updateSummary();
    }

    private List<ItemViewModel> buildListItems(List<SessionsGroup> groups) {
        if (groups == null || groups.isEmpty())
            return null;

        ArrayList<ItemViewModel> res = new ArrayList<>();
        for (SessionsGroup group : groups)
            res.add(new GroupItemViewModel(mFormatter, mAppPreferences.get(), group));

        return res;
    }

    private List<Long> getSelectedSessionsIds(Set<Long> modelIds) {
        ArrayList<Long> ids = new ArrayList<>();
        List<ItemViewModel> list = mListModel.getValue();
        if (list != null) {
            for (ItemViewModel item : list) {
                if (item instanceof GroupItemViewModel && modelIds.contains(item.getId()))
                    ((GroupItemViewModel) item).appendSessionIds(ids);
            }
        }

        return ids;
    }


    void fillFakeSessions() {
        DateTime day = new DateTime(System.currentTimeMillis())
                .minusYears(1)
                .monthOfYear()
                .withMinimumValue()
                .withTime(8, 0, 0, 0);

        ArrayList<Session> list = new ArrayList<>();
        Random rnd = new Random();
        while (day.getMillis() < System.currentTimeMillis()) {
            long start = day.withTime(8, 0, 0, 0).getMillis() / 1000;
            long end = day.withTime(16 + rnd.nextInt(1),
                    rnd.nextInt(60), 0, 0).getMillis() / 1000;

            list.add(new Session(0, start, end));
            day = day.plusDays(1);
            if (day.getDayOfWeek() == 6)
                day = day.plusDays(2);
        }
        mRepository.get().appendAll(list);
    }

    static class DateRange {
        long start;
        long end;

        DateRange(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
