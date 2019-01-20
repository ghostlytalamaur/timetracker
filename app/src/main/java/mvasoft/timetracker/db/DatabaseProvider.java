package mvasoft.timetracker.db;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.processors.BehaviorProcessor;
import timber.log.Timber;


@Singleton
public class DatabaseProvider {

    private BehaviorProcessor<AppDatabase> mDatabaseObservable;

    @Inject
    public DatabaseProvider(Context context) {
        mDatabaseObservable = BehaviorProcessor.createDefault(AppDatabase.getDatabase(context));
        Timber.d("Create DatabaseProvider");
    }

    public BehaviorProcessor<AppDatabase> getDatabase() {
        return mDatabaseObservable;
    }

    public void reinitDatabase(Context context) {
        if (mDatabaseObservable.getValue().isOpen())
            mDatabaseObservable.getValue().close();

        mDatabaseObservable.onNext(AppDatabase.getDatabase(context));
    }
}
