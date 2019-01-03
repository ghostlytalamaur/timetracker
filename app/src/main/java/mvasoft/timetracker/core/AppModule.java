package mvasoft.timetracker.core;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;

@SuppressWarnings("unused")
@Module(includes = {ViewModelModule.class})
abstract class AppModule {

    @Binds
    abstract Context bindApplicationContext(TimeTrackerApp app);

    @Binds
    @Singleton
    abstract Application application(TimeTrackerApp app);
}
