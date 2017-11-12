package mvasoft.timetracker.core;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;


public class TimeTrackerApp extends DaggerApplication {

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this);
    }
}
