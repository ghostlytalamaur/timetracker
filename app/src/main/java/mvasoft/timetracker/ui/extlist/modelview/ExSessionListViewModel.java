package mvasoft.timetracker.ui.extlist.modelview;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.Transformations;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.common.CalculatedLiveData;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.databinding.recyclerview.BaseItemModel;
import mvasoft.timetracker.databinding.recyclerview.ListItemHelper;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.extlist.model.DayGroupListModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

import static mvasoft.timetracker.common.Const.LOG_TAG;


public class ExSessionListViewModel extends BaseViewModel {


    private final Handler mHandler;
    private ScheduledFuture<?> mUpdateFuture;
    private DayGroupListModel mModel;
    private final DateTimeFormatters mFormatter;
    private LiveData<List<BaseItemModel>> mListModel;
    private ScheduledExecutorService mUpdateExecutor;

    private CalculatedLiveData<List<DayGroup>, String> mSummaryTimeLiveData;
    private final CalculatedLiveData<List<DayGroup>, Long> mTargetDiffLiveData;
    private final LiveData<String> mTargetDiffStrLiveData;
    private final LiveData<Boolean> mIsTargetAchieved;

    private final Lazy<DataRepository> mRepository;

    @Inject
    ExSessionListViewModel(@NonNull Application application, Lazy<DataRepository> repository,
                           Lazy<AppPreferences> appPreferences) {
        super(application);
        Log.d(LOG_TAG, "creating ExSessionListViewModel");

        mFormatter = new DateTimeFormatters();
        mHandler = new Handler();
        mModel = new DayGroupListModel(appPreferences, repository);
        mRepository = repository;
        mUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        mModel.getItems().observeForever(sessions -> updateTimer());

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
        Log.d(LOG_TAG, "ExSessionListViewModel.resume()");
        updateTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause() {
        Log.d(LOG_TAG, "ExSessionListViewModel.pause()");

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
            mUpdateFuture = mUpdateExecutor.scheduleWithFixedDelay(() ->
                    mHandler.post(this::updateRunningItems), 0, 1, TimeUnit.SECONDS);
    }

    private void updateRunningItems() {
        if (mListModel == null || mListModel.getValue() == null)
            return;

        List<? extends DayGroup> list = mModel.getItems().getValue();
        if (list == null)
            return;

        for (DayGroup s : list) {
            if (s.isRunning()) {
                BaseItemModel vm = getSessionViewModel(s.getId());
                if (vm != null)
                    vm.dataChanged();
            }
        }

        if (mSummaryTimeLiveData != null)
            mSummaryTimeLiveData.invalidateValue();
        if (mTargetDiffLiveData != null)
            mTargetDiffLiveData.invalidateValue();
    }

    private BaseItemModel getSessionViewModel(long id) {
        if (mListModel == null || mListModel.getValue() == null)
            return null;

        for (BaseItemModel item : mListModel.getValue())
            if (item.getId() == id)
                return item;

        return null;
    }

    public LiveData<List<BaseItemModel>> getListModel() {
        if (mListModel == null) {
            mListModel = Transformations.map(mModel.getItems(), list -> {
                return buildListItem();
            });
        }
        return mListModel;
    }

    private List<BaseItemModel> buildListItem() {
        List<DayGroup> list = mModel.getItems().getValue();
        if (list == null ||  list.isEmpty())
            return null;

        // TODO: use different items for group and single session
        ArrayList<BaseItemModel> res = new ArrayList<>();
        if (mModel.isSingleDay()) {
            DayGroup group = list.get(0);
            for (Session item : group.getSessions())
                res.add(new SessionItemViewModel(mFormatter, item));
        }
        else {
            for (DayGroup item : mModel.getItems().getValue())
                res.add(new SessionItemViewModel(mFormatter, item));
        }
        return res;
    }

    public int getSelectedItemsCount() {
        int res = 0;
        for (BaseItemModel ignored : ListItemHelper.getSelectedItemsIter(mListModel.getValue()))
            res++;
        return res;
    }

    @Override
    protected void onCleared() {
        Log.d(LOG_TAG, "ExSessionListViewModel.onCleared()");
        if (mListModel != null && mListModel.getValue() != null)
            for (BaseItemModel item : mListModel.getValue())
                item.onCleared();

        super.onCleared();
    }

    public void deselectAll() {
        ListItemHelper.deselectAll(getListModel().getValue());
    }

    public LiveData<Integer> deleteSelected() {
        return mRepository.get().deleteSessions(ListItemHelper.getSelectedItemsIds(mListModel.getValue()));
    }


    public void copySelectedToClipboard() {
        final ClipboardManager clipboard = (ClipboardManager)
                getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return;

        StringBuilder text = new StringBuilder();

        for (BaseItemModel item : ListItemHelper.getSelectedItemsIter(getListModel().getValue()))
            if (item instanceof SessionItemViewModel)
                text.append(((SessionItemViewModel) item).asString());

        clipboard.setPrimaryClip(ClipData.newPlainText("Sessions", text.toString()));
    }

    public void setDate(long dateStart, long dateEnd) {
        mModel.setDateRange(dateStart, dateEnd);
    }
}
