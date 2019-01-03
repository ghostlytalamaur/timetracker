package mvasoft.timetracker.core;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.ui.NavigationDrawerActivity;
import mvasoft.timetracker.ui.backup.BackupFragment;
import mvasoft.timetracker.ui.editdate.DatesViewFragment;
import mvasoft.timetracker.ui.editdate.EditDateFragment;
import mvasoft.timetracker.ui.editsession.EditSessionFragment;
import mvasoft.timetracker.ui.extlist.ExSessionListFragment;

@Module
public abstract class FragmentsModule {

    // Разрешаем инжектить зависимости в ExSessionListFragment предоставляемые модулем RepositoryModule
    @ContributesAndroidInjector()
    abstract ExSessionListFragment exSessionListFragmentInjector();


    @ContributesAndroidInjector()
    abstract EditSessionFragment EditSessionFragmentInjector();

    @ContributesAndroidInjector()
    abstract EditDateFragment EditDateFragmentInjector();

    @ContributesAndroidInjector()
    abstract BackupFragment BackupActivityInjector();

    @ContributesAndroidInjector()
    abstract DatesViewFragment DatesViewFragmentInjector();

    @ContributesAndroidInjector()
    abstract NavigationDrawerActivity NavigationDrawerActivityInjector();
}
