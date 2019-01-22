package mvasoft.timetracker.sync;

import android.content.Context;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import mvasoft.timetracker.core.InternalWorkerFactory;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.DatabaseProvider;
import timber.log.Timber;

public class LocalBackupWorker extends Worker {

    private static final String ARGS_IS_BACKUP = "args_is_backup";

    private final Provider<DatabaseProvider> mDatabaseProvider;
    private LocalBackupWorker(@NonNull Context context,
                              @NonNull WorkerParameters workerParams,
                              Provider<DatabaseProvider> databaseProvider) {
        super(context, workerParams);
        mDatabaseProvider = databaseProvider;
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean isBackup = getInputData().getBoolean(ARGS_IS_BACKUP, true);
        Timber.d("Start work. isBackup: %s", isBackup);
        AppDatabase db = mDatabaseProvider.get().getDatabase().getValue();
        try {
            boolean isDone;
            BackupHelper helper = new BackupHelper(db);
            if (isBackup) {
                isDone = helper.backup();
            } else {
                isDone = helper.restore();
            }

            Timber.d("End work. isBackup: %s", isBackup);
            if (isDone)
                return Result.success();
            else
                return Result.failure();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    public static Data makeArgs(boolean isBackup) {
        return new Data.Builder()
                .putBoolean(ARGS_IS_BACKUP, isBackup)
                .build();
    }

    public static class Factory extends InternalWorkerFactory<LocalBackupWorker> {

        private final Provider<DatabaseProvider> mDatabaseProvider;

        @Inject
        Factory(Provider<DatabaseProvider> databaseProvider) {
            mDatabaseProvider = databaseProvider;
        }

        @Override
        public LocalBackupWorker create(Context context, WorkerParameters parameters) {
            return new LocalBackupWorker(context, parameters, mDatabaseProvider);
        }
    }
}
