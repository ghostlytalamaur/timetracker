package mvasoft.timetracker.core;

import android.content.Context;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;


public class DIWorkerFactory extends WorkerFactory {

    private final Map<Class<? extends ListenableWorker>,
            Provider<InternalWorkerFactory<? extends ListenableWorker>>> mWorkers;

    @Inject
    DIWorkerFactory(Map<Class<? extends ListenableWorker>,
            Provider<InternalWorkerFactory<? extends ListenableWorker>>> workers) {
        mWorkers = workers;
    }

    @Nullable
    @Override
    public ListenableWorker createWorker(@NonNull Context appContext,
                                         @NonNull String workerClassName,
                                         @NonNull WorkerParameters workerParameters) {

        try {
            Class<?> clazz = Class.forName(workerClassName);
            Provider<InternalWorkerFactory<? extends ListenableWorker>> provider =
                    mWorkers.get(clazz);
            if (provider == null) {
                throw new IllegalStateException(
                        String.format("Cannot get factory for worker: %s", workerClassName));
            }

            return provider.get().create(appContext, workerParameters);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    String.format("Cannot get class for worker: %s", workerClassName), e);
        }
    }
}
