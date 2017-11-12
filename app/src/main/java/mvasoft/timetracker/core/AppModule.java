package mvasoft.timetracker.core;

import android.content.Context;

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
}
