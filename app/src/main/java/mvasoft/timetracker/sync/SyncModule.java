package mvasoft.timetracker.sync;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface SyncModule {

    @SuppressWarnings("unused")
    @ContributesAndroidInjector
    SyncService syncServiceInjector();

}
