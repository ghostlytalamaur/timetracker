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
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.extlist.model.ExSessionListModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.Session;

import static mvasoft.timetracker.common.Const.LOG_TAG;


public class ExSessionListViewModel extends BaseViewModel {


    private final Handler mHandler;
    private ScheduledFuture<?> mUpdateFuture;
    private ExSessionListModel mModel;
    private final DateTimeFormatters mFormatter;
    private LiveData<List<BaseItemModel>> mListModel;
    private ScheduledExecutorService mUpdateExecutor;

    private CalculatedLiveData<List<Session>, String> mSummaryTimeLiveData;
    private CalculatedLiveData<List<Session>, String> mTargetDiffLiveData;
    private CalculatedLiveData<List<Session>, Boolean> mIsTargetAchieved;

    private final Lazy<DataRepository> mRepository;

    @Inject
    ExSessionListViewModel(@NonNull Application application, Lazy<DataRepository> repository) {
        super(application);
        Log.d(LOG_TAG, "creating ExSessionListViewModel");

        mFormatter = new DateTimeFormatters();
        mHandler = new Handler();
        mModel = new ExSessionListModel(repository);
        mRepository = repository;
        mUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        mModel.getSessionList().observeForever(sessions -> updateTimer());
    }

    public LiveData<String> getSummaryTime() {
        if (mSummaryTimeLiveData == null) {
            mSummaryTimeLiveData = new CalculatedLiveData<>(mModel.getSessionList(), new Function<List<Session>, String>() {
                @Override
                public String apply(List<Session> input) {
                    return mFormatter.formatDuration(mModel.getSummaryTime());
                }
            });
        }
        return mSummaryTimeLiveData;
    }

    public LiveData<String> getTargetDiff() {
        if (mTargetDiffLiveData == null) {
            mTargetDiffLiveData = new CalculatedLiveData<>(mModel.getSessionList(), new Function<List<Session>, String>() {
                @Override
                public String apply(List<Session> input) {
                    return mFormatter.formatDuration(mModel.getTargetDiff());
                }
            });
        }
        return mTargetDiffLiveData;
    }

    public LiveData<Boolean> getIsTargetAchieved() {
        if (mIsTargetAchieved == null)
            mIsTargetAchieved = new CalculatedLiveData<>(mModel.getSessionList(), new Function<List<Session>, Boolean>() {
                @Override
                public Boolean apply(List<Session> input) {
                    return mModel.getTargetDiff() >= 0;
                }
            });
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
        boolean hasOpened = mModel.hasOpenedSessions();
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

        List<Session> list = mModel.getSessionList().getValue();
        if (list == null)
            return;

        for (Session s : list) {
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
            mListModel = Transformations.map(mModel.getSessionList(), list -> {
                if (list.isEmpty())
                    return null;

                ArrayList<BaseItemModel> res = new ArrayList<>();
                for (int i = list.size() - 1; i >= 0; i--)
                    res.add(new SessionItemViewModel(mFormatter, list.get(i)));
                return res;
            });
        }
        return mListModel;
    }

    public int getSelectedItemsCount() {
        return getSelectedItemsIds().size();
    }

    @Override
    protected void onCleared() {
        Log.d(LOG_TAG, "ExSessionListViewModel.onCleared() ");
        if (mListModel != null && mListModel.getValue() != null)
            for (BaseItemModel item : mListModel.getValue())
                item.onCleared();

        super.onCleared();
    }

    public void deselectAll() {
        List<BaseItemModel> list = getListModel().getValue();
        if (list != null) {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getIsSelected())
                    list.get(i).setIsSelected(false);
        }
    }

    public LiveData<Integer> deleteSelected() {
        return mRepository.get().deleteSessions(getSelectedItemsIds());
    }

    private List<Long> getSelectedItemsIds() {
        ArrayList<Long> ids = new ArrayList<>();
        List<BaseItemModel> list = getListModel().getValue();
        if (list != null) {
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getIsSelected())
                    ids.add(list.get(i).getId());
        }
        return ids;
    }

    public void copySelectedToClipboard() {
        List<Session> groups = mModel.getSessionList().getValue();
        if (groups == null)
            return;

        final ClipboardManager clipboard = (ClipboardManager)
                getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return;

        StringBuilder text = new StringBuilder();
        for (Long id : getSelectedItemsIds()) {
            Session session = mModel.getById(id);
            text.append(String.format("%s - %s: %s\n",
                    mFormatter.formatDate(session.getStartTime()),
                    mFormatter.formatDate(session.getEndTime()),
                    mFormatter.formatDuration(session.getDuration())));
        }

        clipboard.setPrimaryClip(ClipData.newPlainText("Sessions", text.toString()));
    }

    public void setDate(long date) {
        mModel.setDate(date);
    }
}
