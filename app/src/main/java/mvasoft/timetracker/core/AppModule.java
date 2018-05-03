package mvasoft.timetracker.core;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.data.RepositoryModule;
import mvasoft.timetracker.deprecated.SessionsContentProvider;
import mvasoft.timetracker.ui.editsession.view.EditSessionActivity;
import mvasoft.timetracker.ui.editsession.view.SessionEditFragment;
import mvasoft.timetracker.ui.extlist.view.ExSessionListFragment;
import mvasoft.timetracker.ui.extlist.view.TabbedActivity;
import mvasoft.timetracker.ui.widget.WidgetModule;

@SuppressWarnings("unused")
@Module(includes = {ViewModelModule.class})
abstract class AppModule {

    @Binds
    abstract Context bindApplicationContext(TimeTrackerApp app);

    @ContributesAndroidInjector(modules = {WidgetModule.class})
    abstract SessionsContentProvider sessionsContentProviderInjector();

    // Разрешаем инжектить зависимости в класс TabbedActivity предоставляемые модулем RepositoryModule
    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract TabbedActivity TabbedActivityInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract ExSessionListFragment exSessionListFragmentInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract EditSessionActivity EditSessionActivityInjector();

    @ContributesAndroidInjector(modules = {RepositoryModule.class})
    abstract SessionEditFragment SessionEditFragmentInjector();

    @Binds
    @Singleton
    abstract Application application(TimeTrackerApp app);
}
