package mvasoft.timetracker.ui.widget;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@SuppressWarnings("unused")
@Module
public abstract class WidgetModule {

    @Binds
    @Singleton
    abstract WidgetHelper bindWidgetHelper(WidgetHelperImpl impl);

    @ContributesAndroidInjector
    abstract SessionsWidgetService sessionsWidgetServiceInjector();

    @ContributesAndroidInjector
    abstract SessionsWidget sessionsWidgetInjector();

}
