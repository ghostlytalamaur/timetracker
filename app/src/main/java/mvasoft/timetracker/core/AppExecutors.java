package mvasoft.timetracker.core;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppExecutors {

    private final Executor mMainThread;
    private final Executor mDiskIO;

    @Inject
    public AppExecutors() {
        mDiskIO = Executors.newSingleThreadExecutor();
        mMainThread = new MainThreadExecutor();
    }


    public Executor getMainThread() {
        return mMainThread;
    }

    public Executor getDiskIO() {
        return mDiskIO;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
