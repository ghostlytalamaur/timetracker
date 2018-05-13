package mvasoft.timetracker.data.room;

import android.arch.lifecycle.LiveData;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class ComputableData<T> extends LiveData<T> {

    private final Executor mMainThreadExecutor;
    private final Executor mExecutor;
    private final Runnable mRefreshRunnable;

    private final AtomicBoolean mInvalid = new AtomicBoolean(true);
    private final AtomicBoolean mComputing = new AtomicBoolean(false);

    // invalidation check always happens on the main thread
    private final Runnable mInvalidationRunnable;


    @Override
    protected void onActive() {
        super.onActive();
        mExecutor.execute(mRefreshRunnable);
    }

    public ComputableData(Executor mainThreadExecutor, Executor executor) {
        mMainThreadExecutor = mainThreadExecutor;
        mExecutor = executor;
        mRefreshRunnable = new RefreshRunnable();
        mInvalidationRunnable = new Runnable() {
            @MainThread
            @Override
            public void run() {
                boolean isActive = hasActiveObservers();
                if (mInvalid.compareAndSet(false, true)) {
                    if (isActive) {
                        mExecutor.execute(mRefreshRunnable);
                    }
                }
            }
        };
    }

    private class RefreshRunnable implements Runnable {

        @WorkerThread
        @Override
        public void run() {
            boolean computed;
            do {
                computed = false;
                // compute can happen only in 1 thread but no reason to lock others.
                if (mComputing.compareAndSet(false, true)) {
                    // as long as it is invalid, keep computing.
                    try {
                        T value = null;
                        while (mInvalid.compareAndSet(true, false)) {
                            computed = true;
                            value = compute();
                        }
                        if (computed) {
                            postValue(value);
                        }
                    } finally {
                        // release compute lock
                        mComputing.set(false);
                    }
                }
                // check invalid after releasing compute lock to avoid the following scenario.
                // Thread A runs compute()
                // Thread A checks invalid, it is false
                // Main thread sets invalid to true
                // Thread B runs, fails to acquire compute lock and skips
                // Thread A releases compute lock
                // We've left invalid in set state. The check below recovers.
            } while (computed && mInvalid.get());
        }

    }

    /**
     * Invalidates the LiveData.
     * <p>
     * When there are active observers, this will trigger a call to {@link #compute()}.
     */
    public void invalidate() {
        mMainThreadExecutor.execute(mInvalidationRunnable);
    }

    @WorkerThread
    protected abstract T compute();
}
