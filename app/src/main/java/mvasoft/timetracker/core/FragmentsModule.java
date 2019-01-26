package mvasoft.timetracker.core;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import mvasoft.timetracker.ui.editdate.DatesViewFragment;
import mvasoft.timetracker.ui.editdate.EditDateFragment;
import mvasoft.timetracker.ui.editsession.EditSessionFragment;
import mvasoft.timetracker.ui.extlist.ExSessionListFragment;

@SuppressWarnings("unused")
@Module
abstract class FragmentsModule {

    @ContributesAndroidInjector()
    abstract ExSessionListFragment exSessionListFragmentInjector();

    @ContributesAndroidInjector()
    abstract EditSessionFragment EditSessionFragmentInjector();

    @ContributesAndroidInjector()
    abstract EditDateFragment EditDateFragmentInjector();

    @ContributesAndroidInjector()
    abstract DatesViewFragment DatesViewFragmentInjector();
}
