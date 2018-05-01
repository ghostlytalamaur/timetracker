package mvasoft.timetracker.viewmodel;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.databinding.Bindable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.ui.DateTimeFormatters;
import mvasoft.timetracker.ui.base.BaseViewModel;

import static mvasoft.timetracker.Consts.LOG_TAG;

public class SessionListViewModel extends BaseViewModel implements LifecycleObserver {

    private final DateTimeFormatters mFormatter;
    private final Handler mHandler;
    private final Runnable mUpdateViewRunnable;
    private Timer mUpdateTimer;
    private GroupsList mTodayGroup;
    private GroupsList mWeekGroup;
    private GroupsList.IGroupsChangesListener mGroupsChangesListener;

    public SessionListViewModel(@NonNull Application application) {
        super(application);

        mFormatter = new DateTimeFormatters();
        mGroupsChangesListener = new GroupsChangesListener();
        mUpdateViewRunnable = new Runnable() {
            @Override
            public void run() {
                notifyPropertyChanged(BR.todayText);
                notifyPropertyChanged(BR.weekText);
            }
        };
        mHandler = new Handler();
    }

    public void setup(GroupsList todayGroup, GroupsList weekGroup) {
        mTodayGroup = todayGroup;
        mWeekGroup = weekGroup;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resume() {
        Log.d(LOG_TAG, "SessionListViewModel.resume()");

        mTodayGroup.addChangesListener(mGroupsChangesListener);
        mWeekGroup.addChangesListener(mGroupsChangesListener);
        updateTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause() {
        Log.d(LOG_TAG, "SessionListViewModel.pause()");

        mTodayGroup.removeChangesListener(mGroupsChangesListener);
        mWeekGroup.removeChangesListener(mGroupsChangesListener);

        stopTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mTodayGroup.swapCursor(null);
        mWeekGroup.swapCursor(null);
    }

    @Bindable
    public String getTodayText() {
        return getGroupText(mTodayGroup);
    }

    @Bindable
    public String getWeekText() {
        return getGroupText(mWeekGroup);
    }

    @Bindable
    public boolean getHasOpenedSessions() {
        return mTodayGroup.hasOpenedSessions();
    }

    private String getGroupText(GroupsList groups) {
        long duration = groups.getDuration();
        if (duration > 0)
            return mFormatter.formatPeriod(duration);
        else
            return null;
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
        boolean hasOpened = mTodayGroup.hasOpenedSessions() || mWeekGroup.hasOpenedSessions();
        if (!hasOpened)
            stopTimer();
        else
            startTimer();
    }

    private class GroupsChangesListener implements GroupsList.IGroupsChangesListener {
        @Override
        public void onDataChanged() {
            notifyPropertyChanged(BR.todayText);
            notifyPropertyChanged(BR.weekText);
            notifyPropertyChanged(BR.hasOpenedSessions);
            updateTimer();
        }
    }

}
