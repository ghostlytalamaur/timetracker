package mvasoft.timetracker.core;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import mvasoft.timetracker.data.RepositoryModule;
import mvasoft.timetracker.preferences.AppPreferenceModule;
import mvasoft.timetracker.ui.widget.WidgetModule;

@Singleton
@Component(modules = {AndroidSupportInjectionModule.class,
        AppModule.class,
        RepositoryModule.class,
        NavigationDrawerActivityModule.class,
        WidgetModule.class,
        AppPreferenceModule.class})
public interface AppComponent extends AndroidInjector<TimeTrackerApp> {

//    void inject(BackupAssistant assistant);

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<TimeTrackerApp> {}
}
