package mvasoft.timetracker.core;

import org.greenrobot.eventbus.EventBus;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import mvasoft.timetracker.TimeTrackerEventBusIndex;


public class TimeTrackerApp extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // TimeTrackerEventBusIndex generated at compile-time by EventBus annotation processor.
        // Better performance and compile-time checks
        EventBus.builder().addIndex(new TimeTrackerEventBusIndex()).installDefaultEventBus();
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this);
    }
}
