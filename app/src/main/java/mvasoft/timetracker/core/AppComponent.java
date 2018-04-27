package mvasoft.timetracker.core;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import mvasoft.timetracker.BackupAssistant;
import mvasoft.timetracker.widget.WidgetModule;

@Singleton
@Component(modules = {AndroidSupportInjectionModule.class,
        AppModule.class,
        WidgetModule.class})
public interface AppComponent extends AndroidInjector<TimeTrackerApp> {

//    void inject(BackupAssistant assistant);

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<TimeTrackerApp> {}
}
