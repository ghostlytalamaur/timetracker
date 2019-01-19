package mvasoft.timetracker.ui.extlist;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
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
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
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
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;
import mvasoft.timetracker.vo.SessionUtils;
import mvasoft.timetracker.vo.SessionsGroup;
import mvasoft.utils.CollectionsUtils;
import timber.log.Timber;


public class ExSessionListViewModel extends BaseViewModel {

    private final DateTimeFormatters mFormatter;
    private final Lazy<AppPreferences> mAppPreferences;
    private final Lazy<DataRepository> mRepository;
    private final ModelSettings mModelSettings;

    private List<SessionsGroup> mGroups;
    private List<DayDescription> mDayDescriptions;

    private long mSummaryTime;
    private long mTargetTime;

    private final MutableLiveData<List<ItemViewModel>> mListModel;
    private final ScheduledExecutorService mUpdateExecutor;
    private final MutableLiveData<String> mSummaryTimeLiveData;
    private final MutableLiveData<String> mTargetDiffStrLiveData;
    private final MutableLiveData<Boolean> mIsTargetAchieved;
    private final MutableLiveData<Boolean> mHasSessions;
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
        mAppPreferences.get(); // create in main thread
        mRepository = repository;
        mModelSettings = new ModelSettings(getApplication());
        mListModel = new MutableLiveData<>();
        mFormatter = new DateTimeFormatters();
        mSummaryTimeLiveData = new MutableLiveData<>();
        mTargetDiffStrLiveData = new MutableLiveData<>();
        mIsTargetAchieved = new MutableLiveData<>(false);
        mHasSessions = new MutableLiveData<>(false);
        mUpdateExecutor = Executors.newSingleThreadScheduledExecutor();

        mGroupType = BehaviorProcessor.createDefault(mModelSettings.getGroupType());
        mDateRange = BehaviorProcessor.createDefault(mModelSettings.getDateRange());

        Flowable<List<Session>> sessions = mDateRange
                .distinctUntilChanged()
                .flatMap(range -> mRepository.get().getSessionsRx(range.start, range.end))
                .doOnNext(list -> Timber.d("New sessions received"));
        Flowable<List<SessionsGroup>> sessionsGroups = Flowable.combineLatest(sessions, mGroupType.distinctUntilChanged(), Pair::new)
                .map(pair -> groupSessions(pair.second, pair.first));

        Flowable<List<DayDescription>> dayDescriptions = mDateRange
                .distinctUntilChanged()
                .flatMap(range -> mRepository.get().getDayDescriptionsRx(range.start, range.end))
                .doOnNext(list -> Timber.d("New day descriptions received"));

        mDisposable = new CompositeDisposable();
        mDisposable.add(sessionsGroups.subscribe(this::setGroups));
        mDisposable.add(dayDescriptions.subscribe(this::setDayDescriptions));
    }

    void setGroupType(SessionsGroup.GroupType groupType) {
        mGroupType.onNext(groupType);
        mModelSettings.setGroupType(groupType);
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
            SessionUtils.sortSessions(list);
            groups.add(new SessionsGroup(list));
        }

        SessionUtils.sortGroups(groups);
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

    public LiveData<Boolean> getHasSessions() {
        return mHasSessions;
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

    DateRange getDateRange() {
        return mDateRange.getValue();
    }

    void setDate(DateRange range) {
        mDateRange.onNext(range);
        mModelSettings.setDateRange(range);
    }

    private void updateTargetTime() {
        mTargetTime = 0;

        DateRange range = mDateRange.getValue();
        DateTime start = new DateTime(range.start * 1000).withTimeAtStartOfDay();
        DateTime end = new DateTime(range.end * 1000).withTime(23, 59, 59, 0);
        int daysCount = Days.daysBetween(start, end).getDays() + 1;
        final HashSet<DateTime> remainingDays = new HashSet<>(daysCount);
        for (int nDay = 0; nDay < daysCount; nDay++) {
            remainingDays.add(start);
            start = start.plusDays(1);
        }

        if (mDayDescriptions != null) {
            for (DayDescription dayDescription : mDayDescriptions) {
                if (dayDescription.isWorkingDay()) {
                    mTargetTime += dayDescription.getTargetDuration() * 60;
                }
                remainingDays.remove(new DateTime(dayDescription.getDate() * 1000).withTimeAtStartOfDay());
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
        mHasSessions.postValue(!CollectionsUtils.isEmpty(groups));
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
        final long start;
        final long end;

        DateRange(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DateRange))
                return false;

            return DateTimeHelper.sameDays(start, ((DateRange) obj).start) &&
                    DateTimeHelper.sameDays(end, ((DateRange) obj).end);
        }
    }

    private static class ModelSettings {

        private static final String PREF_KEY_DATE_RANGE_START = "date_range_start";
        private static final String PREF_KEY_DATE_RANGE_END = "date_range_end";
        private static final String PREF_KEY_GROUP_TYPE = "group_type";
        private final SharedPreferences mPreferences;

        ModelSettings(@NonNull Application application) {
            mPreferences = application.getSharedPreferences(
                    "mvasoft.ExSessionListViewModel", Context.MODE_PRIVATE);
        }

        DateRange getDateRange() {
            long start = mPreferences.getLong(PREF_KEY_DATE_RANGE_START,
                    System.currentTimeMillis() / 1000);
            long end = mPreferences.getLong(PREF_KEY_DATE_RANGE_END, start);
            return new DateRange(start, end);
        }

        void setDateRange(@NonNull DateRange range) {
            mPreferences.edit()
                    .putLong(PREF_KEY_DATE_RANGE_START, range.start)
                    .putLong(PREF_KEY_DATE_RANGE_END, range.end)
                    .apply();
        }

        SessionsGroup.GroupType getGroupType() {
            String value = mPreferences.getString(PREF_KEY_GROUP_TYPE,
                    SessionsGroup.GroupType.gtNone.toString());
            return SessionsGroup.GroupType.valueOf(value);
        }

        void setGroupType(SessionsGroup.GroupType groupType) {
            mPreferences.edit()
                    .putString(PREF_KEY_GROUP_TYPE, groupType.toString())
                    .apply();
        }
    }

}
