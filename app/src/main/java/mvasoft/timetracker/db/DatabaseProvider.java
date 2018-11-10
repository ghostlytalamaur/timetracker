package mvasoft.timetracker.db;

import android.content.Context;

import java.util.EventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import mvasoft.utils.Announcer;


@Singleton
public class DatabaseProvider {

    private AppDatabase mDatabase;
    private Announcer<Observer> mAnnouncer;

    @Inject
    public DatabaseProvider(Context context) {
        mAnnouncer = new Announcer<>(Observer.class);
        reinitDatabase(context);
    }

    public AppDatabase getDatabase() {
        return mDatabase;
    }

    public void reinitDatabase(Context context) {
        if (mDatabase != null && mDatabase.isOpen())
            mDatabase.close();

        mDatabase = AppDatabase.getDatabase(context);
        mAnnouncer.announce().onDatabaseChanged();
    }

    public void addObserver(Observer observer) {
        mAnnouncer.addListener(observer);
    }

    public void removeObserver(Observer observer) {
        mAnnouncer.addListener(observer);
    }

    public interface Observer extends EventListener {
        void onDatabaseChanged();
    }
}
