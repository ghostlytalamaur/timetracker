package mvasoft.timetracker.ui.extlist.modelview;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.Transformations;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.recyclerbinding.viewmodel.ListViewModel;
import mvasoft.timetracker.common.CalculatedLiveData;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.extlist.model.DayGroupListModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;


public class ExSessionListViewModel extends BaseViewModel {


    private final ModelObserver mModelObserver;
    private final Lazy<AppPreferences> mAppPreferences;
    private ScheduledFuture<?> mUpdateFuture;
    private final DayGroupListModel mModel;
    private final DateTimeFormatters mFormatter;
    private final ListViewModel mListModel;
    private final ScheduledExecutorService mUpdateExecutor;

    private CalculatedLiveData<List<DayGroup>, String> mSummaryTimeLiveData;
    private final CalculatedLiveData<List<DayGroup>, Long> mTargetDiffLiveData;
    private final LiveData<String> mTargetDiffStrLiveData;
    private final LiveData<Boolean> mIsTargetAchieved;

    private final Lazy<DataRepository> mRepository;

    @Inject
    ExSessionListViewModel(@NonNull Application application, Lazy<DataRepository> repository,
                           Lazy<AppPreferences> appPreferences) {
        super(application);

        mListModel = new ListViewModel();

        mFormatter = new DateTimeFormatters();
        mAppPreferences = appPreferences;
        mModel = new DayGroupListModel(appPreferences, repository);
        mRepository = repository;
        mUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        mModelObserver = new ModelObserver();
        mModel.getItems().observeForever(mModelObserver);

        mTargetDiffLiveData = new CalculatedLiveData<>(mModel.getItems(),
                input -> mModel.getTargetTimeDiff());

        mTargetDiffStrLiveData = Transformations.map(mTargetDiffLiveData,
                mFormatter::formatDuration);
        mIsTargetAchieved = Transformations.map(mTargetDiffLiveData,
                (target) -> target >= 0);
    }

    public LiveData<String> getSummaryTime() {
        if (mSummaryTimeLiveData == null) {
            mSummaryTimeLiveData = new CalculatedLiveData<>(mModel.getItems(), new Function<List<DayGroup>, String>() {
                @Override
                public String apply(List<DayGroup> input) {
                    return mFormatter.formatDuration(mModel.getSummaryTime());
                }
            });
        }
        return mSummaryTimeLiveData;
    }

    public LiveData<String> getTargetDiff() {
        return mTargetDiffStrLiveData;
    }

    public LiveData<Boolean> getIsTargetAchieved() {
        return mIsTargetAchieved;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resume() {
        updateTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause() {

        if (mUpdateFuture != null) {
            mUpdateFuture.cancel(true);
            mUpdateFuture = null;
        }
    }

    private void updateTimer() {
        boolean hasOpened = mModel.hasRunningSessions();
        if (!hasOpened && mUpdateFuture != null && !mUpdateFuture.isCancelled()) {
            mUpdateFuture.cancel(true);
            mUpdateFuture = null;
        }
        else if (hasOpened && (mUpdateFuture == null || mUpdateFuture.isCancelled()))
            mUpdateFuture = mUpdateExecutor.scheduleWithFixedDelay(this::updateRunningItems,
                    0, 1, TimeUnit.SECONDS);
    }

    private void updateRunningItems() {
        List<ItemViewModel> list = mListModel.getItemsData().getValue();
        if (list == null)
            return;

        for (ItemViewModel item : list) {
            if (item instanceof BaseItemViewModel && ((BaseItemViewModel) item).getIsRunning())
                ((BaseItemViewModel) item).updateDuration();
        }

        if (mSummaryTimeLiveData != null)
            mSummaryTimeLiveData.invalidateValue();
        if (mTargetDiffLiveData != null)
            mTargetDiffLiveData.invalidateValue();
    }

    public ListViewModel getListModel() {
        return mListModel;
    }

    private List<ItemViewModel> buildListItem() {
        List<DayGroup> list = mModel.getItems().getValue();
        if (list == null ||  list.isEmpty())
            return null;

        ArrayList<ItemViewModel> res = new ArrayList<>();
        if (mModel.isSingleDay()) {
            DayGroup group = list.get(0);
            if (group.hasSessions())
                for (Session item : group.getSessions())
                    res.add(new SessionItemViewModel(mFormatter, item));
        }
        else {
            for (DayGroup item : mModel.getItems().getValue())
                if (item.hasSessions())
                    res.add(new DayItemViewModel(mFormatter, item, mAppPreferences.get()));
        }

        return res;
    }

    @Override
    protected void onCleared() {
        if (mUpdateFuture != null && !mUpdateFuture.isCancelled()) {
            mUpdateFuture.cancel(true);
            mUpdateFuture = null;
        }

        mModel.getItems().removeObserver(mModelObserver);
        super.onCleared();
    }

    private List<Long> getSelectedSessionsIds() {
        ArrayList<Long> ids = new ArrayList<>();
        for (ItemViewModel item : mListModel.getSelectedItems()) {
            if (item instanceof BaseItemViewModel)
                ((BaseItemViewModel) item).appendSessionIds(ids);
        }
        return ids;
    }

    public void deleteSelected() {
        mRepository.get().deleteSessions(getSelectedSessionsIds());
    }

    public void copySelectedToClipboard() {
        final ClipboardManager clipboard = (ClipboardManager)
                getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return;

        StringBuilder text = new StringBuilder();

        DateTimeFormatters formatter = new DateTimeFormatters(DateTimeFormatters.DateTimeFormattersType.dtft_Clipboard);
        for (ItemViewModel item : mListModel.getSelectedItems())
            if (item instanceof BaseItemViewModel)
                text.append(((BaseItemViewModel) item).getClipboardString(formatter));


        String str = text.toString();
        if (!str.isEmpty())
            clipboard.setPrimaryClip(ClipData.newPlainText("Sessions", str));
    }

    public void setDate(long dateStart, long dateEnd) {
        mModel.setDateRange(dateStart, dateEnd);
    }

    private class ModelObserver implements Observer<List<DayGroup>> {

        @Override
        public void onChanged(@Nullable List<DayGroup> list) {
            mListModel.setItemsList(buildListItem());
            updateTimer();
        }
    }

}
