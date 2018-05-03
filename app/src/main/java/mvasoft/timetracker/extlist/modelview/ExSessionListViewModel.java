package mvasoft.timetracker.extlist.modelview;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.extlist.model.BaseItemModel;
import mvasoft.timetracker.extlist.model.ExSessionListModel;
import mvasoft.timetracker.ui.DateTimeFormatters;
import mvasoft.timetracker.ui.base.BaseViewModel;

import static mvasoft.timetracker.Consts.LOG_TAG;

public class ExSessionListViewModel extends BaseViewModel {


    private final Handler mHandler;
    private final Runnable mUpdateViewRunnable;
    private final Observer<GroupsList> mGroupsObserver;
    private ExSessionListModel mModel;
    private final DateTimeFormatters mFormatter;
    private MutableLiveData<List<BaseItemModel>> mListModel;
    private Timer mUpdateTimer;

    private final Lazy<DataRepository> mRepository;

    @Inject
    ExSessionListViewModel(@NonNull Application application, Lazy<DataRepository> repository) {
        super(application);
        Log.d(LOG_TAG, "creating ExSessionListViewModel");

        mFormatter = new DateTimeFormatters();
        mUpdateViewRunnable = new Runnable() {
            @Override
            public void run() {
                updateRunningItems();
            }
        };
        mHandler = new Handler();
        mModel = new ExSessionListModel(repository);
        mRepository = repository;
        mGroupsObserver = new Observer<GroupsList>() {
            @Override
            public void onChanged(@Nullable GroupsList groupsList) {
                updateListData();
                updateTimer();
            }
        };
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resume() {
        Log.d(LOG_TAG, "ExSessionListViewModel.resume()");
        updateTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause() {
        Log.d(LOG_TAG, "ExSessionListViewModel.pause()");

        stopTimer();
    }

    private void startTimer() {
        if (mUpdateTimer != null)
            return;

        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mUpdateViewRunnable);
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer.purge();
        }
        mUpdateTimer = null;
    }

    private void updateTimer() {
        GroupsList groups = mModel.getGroups().getValue();
        boolean hasOpened = groups != null && groups.hasOpenedSessions();
        if (!hasOpened)
            stopTimer();
        else
            startTimer();
    }

    private void updateRunningItems() {
        if (mListModel == null || mListModel.getValue() == null)
            return;

        GroupsList groups = mModel.getGroups().getValue();
        if (groups == null)
            return;

        for (int i = 0; i < groups.count(); i++) {
            if (groups.get(i).isRunning())
                // TODO: thinking: is best way to update using SessionGroupViewModel?
                groups.get(i).dataChanged();
        }
    }

    private void clearListModelItems() {
        if (mListModel == null)
            return;
        if (mListModel.getValue() != null) {
            List<BaseItemModel> list = mListModel.getValue();
            for (BaseItemModel item : list)
                item.onCleared();
        }
    }

    private void updateListData() {
        if (mListModel == null)
            return;

        GroupsList groups = mModel.getGroups().getValue();
        if (groups == null)
            return;

        LongSparseArray<BaseItemModel> oldItems = new LongSparseArray<>();
        List<BaseItemModel> oldList = mListModel.getValue();
        if (oldList != null)
            for (int i = 0; i < oldList.size(); i++) {
                BaseItemModel item = oldList.get(i);
                oldItems.append(item.getId(), item);
            }

        HashSet<Long> newIds = new HashSet<>();
        List<BaseItemModel> newList = new ArrayList<>();
        for (int i = 0; i < groups.count(); i++) {
            GroupsList.SessionGroup group = groups.get(i);
            BaseItemModel oldItem = oldItems.get(group.getID(), null);
            if (oldItem == null)
                newList.add(new SessionGroupViewModel(mFormatter, group));
            else
                newList.add(oldItem);

            newIds.add(group.getID());
        }

        for (int i = 0; i < oldItems.size(); i++)
            if (!newIds.contains(oldItems.keyAt(i)))
                oldItems.valueAt(i).onCleared();

        mListModel.setValue(newList);
    }

    public LiveData<List<BaseItemModel>> getListModel() {
        if (mListModel == null) {
            mListModel = new MutableLiveData<>();
            updateListData();
        }
        return mListModel;
    }

    public void setGroupType(GroupType groupType) {
        mModel.setGroupType(groupType);
        mModel.getGroups().observeForever(mGroupsObserver);
    }

    public ExSessionListModel getModel() {
        return mModel;
    }

    public int getSelectedItemsCount() {
        return getSelectedItemsIds().size();
    }

    @Override
    protected void onCleared() {
        Log.d(LOG_TAG, "ExSessionListViewModel.onCleared() with " + mModel.getGroupType().toString());
        clearListModelItems();
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
        return mRepository.get().deleteGroups(mModel.getGroupType(), getSelectedItemsIds());
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
        GroupsList groups = mModel.getGroups().getValue();
        if (groups == null)
            return;

        final ClipboardManager clipboard = (ClipboardManager)
                getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return;

        StringBuilder text = new StringBuilder();
        for (Long id : getSelectedItemsIds()) {
            GroupsList.SessionGroup group = groups.getByID(id);
            switch (mModel.getGroupType()) {
                case gt_Day:
                    text.append(String.format("%s: %s\n",
                            mFormatter.formatDate(group.getStart()),
                            mFormatter.formatDate(group.getDuration())));
                    break;

                default:
                    text.append(String.format("%s - %s: %s\n",
                            mFormatter.formatDate(group.getStart()),
                            mFormatter.formatDate(group.getEnd()),
                            mFormatter.formatPeriod(group.getDuration())));
                    break;
            }
        }

        clipboard.setPrimaryClip(ClipData.newPlainText("Sessions", text.toString()));
    }
}
