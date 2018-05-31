package mvasoft.timetracker.core;

import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import mvasoft.timetracker.TimeTrackerEventBusIndex;


public class TimeTrackerApp extends DaggerApplication {

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
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        TimeTrackerApp app = (TimeTrackerApp) context.getApplicationContext();
        return app.mRefWatcher;
    }
}
