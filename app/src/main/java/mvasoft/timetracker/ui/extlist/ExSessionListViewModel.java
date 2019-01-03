package mvasoft.timetracker.ui.extlist;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.disposables.CompositeDisposable;
import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.recyclerbinding.viewmodel.ListViewModel;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;


public class ExSessionListViewModel extends BaseViewModel {

    private final Lazy<AppPreferences> mAppPreferences;
    private final DayGroupListModel mData;
    private final DateTimeFormatters mFormatter;
    private final ListViewModel mListModel;
    private final ScheduledExecutorService mUpdateExecutor;
    private final LiveData<String> mSummaryTimeLiveData;
    private final LiveData<String> mTargetDiffStrLiveData;
    private final LiveData<Boolean> mIsTargetAchieved;
    private final Lazy<DataRepository> mRepository;
    private final CompositeDisposable mDisposable;
    private ScheduledFuture<?> mUpdateFuture;
    private LiveData<Boolean> mOpenedSessionId;


    @Inject
    ExSessionListViewModel(@NonNull Application application, Lazy<DataRepository> repository,
                           Lazy<AppPreferences> appPreferences) {
        super(application);

        mAppPreferences = appPreferences;
        mRepository = repository;

        mListModel = new ListViewModel();
        mFormatter = new DateTimeFormatters();
        mData = new DayGroupListModel(appPreferences, repository);
        mUpdateExecutor = Executors.newSingleThreadScheduledExecutor();

        mTargetDiffStrLiveData = LiveDataReactiveStreams.fromPublisher(
                mData.getTargetTimeDiffData()
                        .map(mFormatter::formatDuration));

        mIsTargetAchieved = LiveDataReactiveStreams.fromPublisher(
                mData.getTargetTimeDiffData()
                        .map(diff -> diff >= 0));

        mSummaryTimeLiveData = LiveDataReactiveStreams.fromPublisher(
                mData.getSummaryTimeData()
                        .map(mFormatter::formatDuration)
        );
        mDisposable = new CompositeDisposable();
        mDisposable.add(mData.getItems()
                .subscribe(groups -> mListModel.setItemsList(buildListItem(groups))));
        mDisposable.add(mData.hasRunningSessionsData()
                .subscribe(this::updateTimer));
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

    public LiveData<Boolean> getOpenedSessionId() {
        if (mOpenedSessionId == null)
            mOpenedSessionId = LiveDataReactiveStreams.fromPublisher(
                    mRepository.get().getOpenedSessionsIds().map(list -> list.size() > 0));

        return mOpenedSessionId;
    }

    public ListViewModel getListModel() {
        return mListModel;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
            mUpdateFuture = null;
        }
    }

    void toggleSession() {
        mRepository.get().toggleSession();
    }

    @Override
    protected void onCleared() {
        mData.dispose();
        mDisposable.dispose();
        super.onCleared();
    }

    void deleteSelected() {
        mRepository.get().deleteSessions(getSelectedSessionsIds());
    }

    boolean copySelectedToClipboard() {
        final ClipboardManager clipboard = (ClipboardManager)
                getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return false;

        StringBuilder text = new StringBuilder();

        DateTimeFormatters formatter = new DateTimeFormatters(
                DateTimeFormatters.DateTimeFormattersType.dtft_Clipboard);

        List<ItemViewModel> list = mListModel.getItemsData().getValue();
        if (list == null)
            return false;

        for (ItemViewModel item : list) {
            if (item instanceof BaseItemViewModel && item.isSelected())
                text.append(((BaseItemViewModel) item).getClipboardString(formatter));
        }


        String str = text.toString();
        if (!str.isEmpty()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Sessions", str));
            return true;
        }

        return false;
    }

    public void saveState(Bundle outState) {
        mListModel.saveState(outState);
    }

    public void restoreState(Bundle state) {
        mListModel.restoreState(state);
    }

    public void setDate(long dateStart, long dateEnd) {
        mData.setDateRange(dateStart, dateEnd);
    }

    private void updateTimer(Boolean hasRunning) {
        if (hasRunning != null && !hasRunning) {
            if (mUpdateFuture != null)
                mUpdateFuture.cancel(true);
            mUpdateFuture = null;
            mData.invalidateValues();

        } else if (mUpdateFuture == null) {
            mUpdateFuture = mUpdateExecutor.scheduleWithFixedDelay(this::updateRunningItems,
                    0, 1, TimeUnit.SECONDS);
        }
    }

    private void updateRunningItems() {
        List<ItemViewModel> list = mListModel.getItemsData().getValue();
        if (list != null)
            for (ItemViewModel item : list) {
                if (item instanceof BaseItemViewModel && ((BaseItemViewModel) item).getIsRunning())
                    ((BaseItemViewModel) item).updateDuration();
            }

        mData.invalidateValues();
    }

    private List<ItemViewModel> buildListItem(List<DayGroup> groups) {
        if (groups == null || groups.isEmpty())
            return null;

        ArrayList<ItemViewModel> res = new ArrayList<>();
        if (mData.isSingleDay()) {
            DayGroup group = groups.get(0);
            if (group.hasSessions())
                for (Session item : group.getSessions())
                    res.add(new SessionItemViewModel(mFormatter, item));
        } else {
            for (DayGroup item : groups)
                if (item.hasSessions())
                    res.add(new DayItemViewModel(mFormatter, item, mAppPreferences.get()));
        }

        return res;
    }

    private List<Long> getSelectedSessionsIds() {
        ArrayList<Long> ids = new ArrayList<>();
        List<ItemViewModel> list = mListModel.getItemsData().getValue();
        if (list != null) {
            for (ItemViewModel item : list) {
                if (item instanceof BaseItemViewModel && item.isSelected())
                    ((BaseItemViewModel) item).appendSessionIds(ids);
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
}
