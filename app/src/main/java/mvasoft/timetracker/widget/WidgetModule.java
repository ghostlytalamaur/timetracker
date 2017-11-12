package mvasoft.timetracker.widget;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class WidgetModule {

    @Binds
    @Singleton
    abstract IWidgetHelper bindWidgetHelper(WidgetHelper impl);
}
