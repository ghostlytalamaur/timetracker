package mvasoft.timetracker.core;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.data.RepositoryModule;
import mvasoft.timetracker.ui.backup.BackupActivity;
import mvasoft.timetracker.ui.editdate.EditDateActivity;
import mvasoft.timetracker.ui.editdate.EditDateFragment;
import mvasoft.timetracker.ui.editsession.EditSessionActivity;
import mvasoft.timetracker.ui.editsession.EditSessionFragment;
import mvasoft.timetracker.ui.extlist.ExSessionListFragment;
import mvasoft.timetracker.ui.extlist.TabbedActivity;
import mvasoft.timetracker.ui.widget.SessionsWidgetService;

@SuppressWarnings("unused")
@Module(includes = {ViewModelModule.class})
abstract class AppModule {

    @Binds
    abstract Context bindApplicationContext(TimeTrackerApp app);

//    @ContributesAndroidInjector(modules = {WidgetModule.class})
//    abstract SessionsContentProvider sessionsContentProviderInjector();

    // Разрешаем инжектить зависимости в класс TabbedActivity предоставляемые модулем RepositoryModule
    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract TabbedActivity TabbedActivityInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract ExSessionListFragment exSessionListFragmentInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract EditSessionActivity EditSessionActivityInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract EditSessionFragment EditSessionFragmentInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract EditDateActivity EditDateActivityInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract EditDateFragment EditDateFragmentInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract BackupActivity BackupActivityInjector();

    @Binds
    @Singleton
    abstract Application application(TimeTrackerApp app);
}
