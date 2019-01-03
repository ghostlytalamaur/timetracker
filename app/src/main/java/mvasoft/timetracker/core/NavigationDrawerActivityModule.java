package mvasoft.timetracker.core;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.ui.NavigationDrawerActivity;
import mvasoft.timetracker.ui.common.NavigationController;
import mvasoft.timetracker.ui.common.NavigationControllerImpl;

@Module
public abstract class NavigationDrawerActivityModule {

    @ContributesAndroidInjector(modules = {FragmentsModule.class})
    abstract NavigationDrawerActivity navigationDrawerActivityInjector();

    @Binds
    abstract NavigationController provideNavigationController(NavigationControllerImpl impl);

}
