package mvasoft.timetracker.core;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;
import mvasoft.timetracker.BuildConfig;
import mvasoft.timetracker.TimeTrackerEventBusIndex;
import mvasoft.timetracker.events.SessionToggledEvent;
import mvasoft.timetracker.events.SessionsDeletedEvent;
import mvasoft.timetracker.ui.widget.WidgetHelper;
import timber.log.Timber;


public class TimeTrackerApp extends Application
        implements HasActivityInjector,
        HasBroadcastReceiverInjector,
        HasServiceInjector
{

    @Inject
    WidgetHelper mWidgetHelper;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> dispatchingReceiverInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    private RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        // TimeTrackerEventBusIndex generated at compile-time by EventBus annotation processor.
        // Better performance and compile-time checks
        EventBus.builder().addIndex(new TimeTrackerEventBusIndex()).installDefaultEventBus();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        mRefWatcher = LeakCanary.install(this);
        EventBus.getDefault().register(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugPrefixTree());
        }

        AppInjector.init(this);
        Timber.d("create TimeTrackerApp");
    }

    @Override
    public void onTerminate() {
        EventBus.getDefault().unregister(this);
        super.onTerminate();
    }

    public static RefWatcher getRefWatcher(Context context) {
        TimeTrackerApp app = (TimeTrackerApp) context.getApplicationContext();
        return app.mRefWatcher;
    }

    @Subscribe
    public void onSessionsDeleted(SessionsDeletedEvent e) {
        mWidgetHelper.updateWidget();
    }

    @Subscribe
    public void onSessionToggledEvent(SessionToggledEvent e) {
        mWidgetHelper.updateWidget();
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return dispatchingReceiverInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }

    public class DebugPrefixTree extends Timber.DebugTree {
        @Override
        protected void log(int priority, String tag, @NotNull String message, Throwable t) {
            super.log(priority, "timberlog." + tag, message, t);
        }
    }
}
