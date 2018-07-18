package mvasoft.timetracker.ui.widget;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.data.RepositoryModule;

@SuppressWarnings("unused")
@Module
public abstract class WidgetModule {

    @Binds
    @Singleton
    abstract WidgetHelper bindWidgetHelper(WidgetHelperImpl impl);

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract SessionsWidgetService sessionsWidgetServiceInjector();

    @ContributesAndroidInjector
    abstract SessionsWidget sessionsWidgetInjector();

}
