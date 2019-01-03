package mvasoft.timetracker.core;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.data.RepositoryModule;
import mvasoft.timetracker.ui.backup.BackupFragment;
import mvasoft.timetracker.ui.NavigationDrawerActivity;
import mvasoft.timetracker.ui.editdate.DatesViewFragment;
import mvasoft.timetracker.ui.editdate.EditDateFragment;
import mvasoft.timetracker.ui.editsession.EditSessionFragment;
import mvasoft.timetracker.ui.extlist.ExSessionListFragment;

@SuppressWarnings("unused")
@Module(includes = {ViewModelModule.class})
abstract class AppModule {

    @Binds
    abstract Context bindApplicationContext(TimeTrackerApp app);

    // Разрешаем инжектить зависимости в ExSessionListFragment предоставляемые модулем RepositoryModule
    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract ExSessionListFragment exSessionListFragmentInjector();


    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract EditSessionFragment EditSessionFragmentInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract EditDateFragment EditDateFragmentInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract BackupFragment BackupActivityInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract DatesViewFragment DatesViewFragmentInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract NavigationDrawerActivity NavigationDrawerActivityInjector();

    @Binds
    @Singleton
    abstract Application application(TimeTrackerApp app);
}
