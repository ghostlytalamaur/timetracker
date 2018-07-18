package mvasoft.timetracker.core;

import android.content.Context;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import mvasoft.timetracker.BuildConfig;
import mvasoft.timetracker.TimeTrackerEventBusIndex;
import mvasoft.timetracker.data.event.SessionToggledEvent;
import mvasoft.timetracker.ui.common.EventBusSupportActivity;
import mvasoft.timetracker.ui.widget.SessionsWidgetService;
import mvasoft.timetracker.ui.widget.WidgetHelper;
import timber.log.Timber;


public class TimeTrackerApp extends DaggerApplication {

    @Inject
    WidgetHelper mWidgetHelper;

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

        Timber.d("create TimeTrackerApp");
    }

    @Override
    public void onTerminate() {
        EventBus.getDefault().unregister(this);
        super.onTerminate();
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        TimeTrackerApp app = (TimeTrackerApp) context.getApplicationContext();
        return app.mRefWatcher;
    }

    @Subscribe
    public void onSessionToggledEvent(SessionToggledEvent e) {
        mWidgetHelper.updateWidget();
    }

    public class DebugPrefixTree extends Timber.DebugTree {
        @Override
        protected void log(int priority, String tag, @NotNull String message, Throwable t) {
            super.log(priority, "timberlog." + tag, message, t);
        }
    }
}
