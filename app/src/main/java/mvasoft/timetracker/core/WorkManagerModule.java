package mvasoft.timetracker.core;

import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import mvasoft.timetracker.sync.LocalBackupWorker;

@SuppressWarnings("unused")
@Module
abstract class WorkManagerModule {

    @Binds
    @IntoMap
    @WorkerKey(LocalBackupWorker.class)
    abstract InternalWorkerFactory<? extends ListenableWorker> bindLocalBackupWorker(
            LocalBackupWorker.Factory factory);

    @Binds
    abstract WorkerFactory bindWorkerFactory(DIWorkerFactory factory);
}
