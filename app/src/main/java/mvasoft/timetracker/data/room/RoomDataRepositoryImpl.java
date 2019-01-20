package mvasoft.timetracker.data.room;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import io.reactivex.Flowable;
import mvasoft.timetracker.core.AppExecutors;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.DatabaseProvider;
import mvasoft.timetracker.events.DayDescriptionSavedEvent;
import mvasoft.timetracker.events.SessionSavedEvent;
import mvasoft.timetracker.events.SessionToggledEvent;
import mvasoft.timetracker.events.SessionsDeletedEvent;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;
import timber.log.Timber;

@Singleton
public class RoomDataRepositoryImpl implements DataRepository {

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
                new SessionsDeletedEvent(mDatabaseProvider.getDatabase().getValue()
                        .groupsModel().deleteByIds(ids))));
    }

    @Override
    public Flowable<List<Long>> getOpenedSessionsIds() {

        return mDatabaseProvider
                .getDatabase()
                .flatMap(db -> db.groupsModel().getOpenedSessionsIds());
    }

    @Override
    public void toggleSession() {
        mExecutors.getDiskIO().execute(() -> {
            Timber.d("Try close opened session.");
            AppDatabase db = mDatabaseProvider.getDatabase().getValue();
            int updatedRows = db.groupsModel().closeOpenedSessions();
            ToggleSessionResult res;
            if (updatedRows != 0) {
                res = ToggleSessionResult.tgs_Stopped;
                Timber.d("%d sessions stopped.", updatedRows);
            }
            else {
                Timber.d("No opened session. Start new session.");
                Session session = new Session(0, System.currentTimeMillis() / 1000L, 0);
                if (db.groupsModel().appendSession(session) > 0) {
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
                new SessionSavedEvent(mDatabaseProvider.getDatabase().getValue()
                        .groupsModel().updateSession(session) > 0)));
    }

    public LiveData<Session> getSessionById(long id) {
        return LiveDataReactiveStreams.fromPublisher(mDatabaseProvider.getDatabase()
                .flatMap(db -> db.groupsModel().getSessionByIdRx(id)));
    }

    @Override
    public Flowable<List<Long>> getSessionsIdsRx() {
        return mDatabaseProvider
                .getDatabase()
                .flatMap(db -> db.groupsModel().getSessionsIdsRx());
    }

    @Override
    public Flowable<DayDescription> getDayDescriptionRx(Long date) {
        return mDatabaseProvider
                .getDatabase()
                .flatMap(db -> db.groupsModel().getDayDescriptionRx(date));
    }

    @Override
    public Flowable<List<DayDescription>> getDayDescriptionsRx(Long start, Long end) {
        return mDatabaseProvider
                .getDatabase()
                .flatMap(db -> db.groupsModel().getDayDescriptionsRx(start, end));
    }

    @Override
    public void updateDayDescription(DayDescription dayDescription) {
        mExecutors.getDiskIO().execute(() -> {
            AppDatabase db = mDatabaseProvider.getDatabase().getValue();
            boolean wasSaved = db.groupsModel().updateDayDescription(dayDescription) != 0;
            if (!wasSaved)
                wasSaved = db.groupsModel().appendDayDescription(dayDescription) > 0;
            EventBus.getDefault().post(new DayDescriptionSavedEvent(wasSaved));
        });
    }

    @Override
    public Flowable<List<Session>> getSessionsRx(long start, long end) {
        return mDatabaseProvider
                .getDatabase()
                .flatMap(db -> db.groupsModel().getSessionsRx(start, end));
    }

    @Override
    public void appendAll(ArrayList<Session> list) {
        mExecutors.getDiskIO().execute(() -> mDatabaseProvider.getDatabase().getValue()
                .groupsModel().appendAll(list));
    }
}
