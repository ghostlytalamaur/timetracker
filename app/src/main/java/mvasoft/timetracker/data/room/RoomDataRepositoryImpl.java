package mvasoft.timetracker.data.room;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import mvasoft.timetracker.core.AppExecutors;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.events.DayDescriptionSavedEvent;
import mvasoft.timetracker.events.SessionSavedEvent;
import mvasoft.timetracker.events.SessionToggledEvent;
import mvasoft.timetracker.events.SessionsDeletedEvent;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.DatabaseProvider;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;
import timber.log.Timber;

@Singleton
public class RoomDataRepositoryImpl implements DataRepository {

    private static final Object NOTHING = new Object();

    private final AppExecutors mExecutors;
    private final DatabaseProvider mDatabaseProvider;

    @Inject
    RoomDataRepositoryImpl(AppExecutors executors, DatabaseProvider dbProvider) {
        mExecutors = executors;
        mDatabaseProvider = dbProvider;
    }

    @Override
    public void deleteSessions(List<Long> ids) {
        mExecutors.getDiskIO().execute(() -> EventBus.getDefault().post(
                new SessionsDeletedEvent(getDb().groupsModel().deleteByIds(ids))));
    }

    @Override
    public Flowable<List<Long>> getOpenedSessionsIds() {

        return createFlowable(() ->
            getDb().groupsModel().getOpenedSessionsIds()
        );
    }

    @Override
    public void toggleSession() {
        mExecutors.getDiskIO().execute(() -> {
            Timber.d("Try close opened session.");
            int updatedRows = getDb().groupsModel().closeOpenedSessions();
            ToggleSessionResult res;
            if (updatedRows != 0) {
                res = ToggleSessionResult.tgs_Stopped;
                Timber.d("%d sessions stopped.", updatedRows);
            }
            else {
                Timber.d("No opened session. Start new session.");
                Session session = new Session(0, System.currentTimeMillis() / 1000L, 0);
                if (getDb().groupsModel().appendSession(session) > 0) {
                    res = ToggleSessionResult.tgs_Started;
                    Timber.d("New session started.");
                }
                else {
                    res = ToggleSessionResult.tgs_Error;
                    Timber.d("Cannot start new session.");
                }
            }

            EventBus.getDefault().post(new SessionToggledEvent(res));
        });

    }

    @Override
    public void updateSession(Session session) {
        mExecutors.getDiskIO().execute(() -> EventBus.getDefault().post(
                new SessionSavedEvent(getDb().groupsModel().updateSession(session) > 0)));
    }

    public LiveData<Session> getSessionById(long id) {
        return createLiveData(() -> getDb().groupsModel().getSessionById(id));
    }

    @Override
    public Flowable<List<Long>> getSessionsIdsRx() {
        return createFlowable(() -> getDb().groupsModel().getSessionsIdsRx());
    }

    @Override
    public Flowable<DayDescription> getDayDescriptionRx(Long date) {
        return createFlowable(() -> getDb().groupsModel().getDayDescriptionRx(date));
    }

    @Override
    public Flowable<List<DayDescription>> getDayDescriptionsRx(Long start, Long end) {
        return createFlowable(() -> getDb().groupsModel().getDayDescriptionsRx(start, end));
    }

    @Override
    public void updateDayDescription(DayDescription dayDescription) {
        mExecutors.getDiskIO().execute(() -> {
            boolean wasSaved = getDb().groupsModel().updateDayDescription(dayDescription) != 0;
            if (!wasSaved)
                wasSaved = getDb().groupsModel().appendDayDescription(dayDescription) > 0;
            EventBus.getDefault().post(new DayDescriptionSavedEvent(wasSaved));
        });
    }

    @Override
    public Flowable<List<Session>> getSessionsRx(long start, long end) {
        return createFlowable(() -> getDb().groupsModel().getSessionsRx(start, end));
    }

    @Override
    public Flowable<List<DayGroup>> getDayGroupsRx(List<Long> days) {
        return createFlowable(() -> getDb().groupsModel().getDayGroupsRx(days));
    }

    @Override
    public void appendAll(ArrayList<Session> list) {
        mExecutors.getDiskIO().execute(() -> getDb().groupsModel().appendAll(list));
    }

    private Flowable<Object> createFlowable() {
        return Flowable.create(emitter -> {
            final DatabaseProvider.Observer observer = new DatabaseProvider.Observer() {
                @Override
                public void onDatabaseChanged() {
                    Timber.d("Database changed with Flowable.");
                    if (!emitter.isCancelled())
                        emitter.onNext(NOTHING);
                }
            };

            if (!emitter.isCancelled()) {
                mDatabaseProvider.addObserver(observer);
                emitter.setDisposable(Disposables.fromAction(new Action() {
                    @Override
                    public void run() {
                        mDatabaseProvider.removeObserver(observer);
                    }
                }));
            }

            // emit once to avoid missing any data and also easy chaining
            if (!emitter.isCancelled()) {
                emitter.onNext(NOTHING);
            }
        }, BackpressureStrategy.LATEST);
    }

    private <T> Flowable<T> createFlowable(@NonNull Callable<Flowable<T>> callable) {
        return createFlowable()
                .flatMap(object -> callable.call());
    }

    private <T> LiveData<T> createLiveData(@NonNull Callable<LiveData<T>> callable) {
        return Transformations.switchMap(new DbObserverLiveData(mDatabaseProvider),
                (input) -> {
                    try {
                        return callable.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return  null;
                    }
                });
    }

    private AppDatabase getDb() {
        return mDatabaseProvider.getDatabase();
    }


    static class DbObserverLiveData extends LiveData<Object> {

        final DatabaseProvider mProvider;
        private final DatabaseProvider.Observer mObserver;

        DbObserverLiveData(DatabaseProvider provider) {
            mProvider = provider;
            mObserver = new DatabaseProvider.Observer() {
                @Override
                public void onDatabaseChanged() {
                    Timber.d("Database changed with LiveData.");
                    setValue(NOTHING);
                }
            };
        }

        @Override
        protected void onActive() {
            super.onActive();
            mProvider.addObserver(mObserver);
            setValue(NOTHING);
        }

        @Override
        protected void onInactive() {
            mProvider.removeObserver(mObserver);
            super.onInactive();
        }
    }
}
