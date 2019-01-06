package mvasoft.timetracker.core;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.ui.NavigationDrawerActivity;

@Module
public abstract class NavigationDrawerActivityModule {

    @ContributesAndroidInjector(modules = {FragmentsModule.class})
    abstract NavigationDrawerActivity navigationDrawerActivityInjector();

}
