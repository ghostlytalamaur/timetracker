package mvasoft.timetracker.core;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.data.SessionsContentProvider;
import mvasoft.timetracker.widget.WidgetModule;

@Module
public abstract class AppModule {

    @Binds
    abstract Context bindApplicationContext(TimeTrackerApp app);

    @ContributesAndroidInjector(modules = {WidgetModule.class})
    abstract SessionsContentProvider sessionsContentProviderInjector();

    @Binds
    @Singleton
    abstract Application application(TimeTrackerApp app);
}
