package mvasoft.timetracker.core;

import android.content.Context;

import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

public abstract class InternalWorkerFactory<T extends ListenableWorker> {

    public abstract T create(Context context, WorkerParameters parameters);
}
