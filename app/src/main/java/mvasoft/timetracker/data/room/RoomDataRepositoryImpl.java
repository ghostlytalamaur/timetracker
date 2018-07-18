package mvasoft.timetracker.data.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.InvalidationTracker;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Flowable;
import mvasoft.timetracker.core.AppExecutors;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.data.event.DayDescriptionSavedEvent;
import mvasoft.timetracker.data.event.SessionSavedEvent;
import mvasoft.timetracker.data.event.SessionToggledEvent;
import mvasoft.timetracker.data.event.SessionsDeletedEvent;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.SessionsDao;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;
import timber.log.Timber;

@Singleton
public class RoomDataRepositoryImpl implements DataRepository {

    private final AppExecutors mExecutors;
    private SessionsDao mGroupsModel;
    private AppDatabase mDatabase;

    @Inject
    RoomDataRepositoryImpl(Application application, AppExecutors executors) {
        mExecutors = executors;
        reinitDatabase(application);
    }

    public void reinitDatabase(Application application) {
        if (mDatabase != null)
            mDatabase.close();

        mDatabase = AppDatabase.getDatabase(application);
        mGroupsModel = mDatabase.groupsModel();
    }

    @Override
    public LiveData<List<Session>> getSessions() {
        return mGroupsModel.getAll();
    }

    @Override
    public void deleteSessions(List<Long> ids) {
        mExecutors.getDiskIO().execute(() -> EventBus.getDefault().post(
                new SessionsDeletedEvent(mDatabase.groupsModel().deleteByIds(ids))));
    }

    @Override
    public LiveData<Long> getOpenedSessionId() {
        return mGroupsModel.getOpenedSessionId();
    }

    @Override
    public Flowable<List<Long>> getOpenedSessionsIds() {
        return mGroupsModel.getOpenedSessionsIds();
    }

    @Override
    public void toggleSession() {
        mExecutors.getDiskIO().execute(() -> {
            Timber.d("Try close opened session.");
            int updatedRows = mGroupsModel.closeOpenedSessions();
            ToggleSessionResult res;
            if (updatedRows != 0) {
                res = ToggleSessionResult.tgs_Stopped;
                Timber.d("%d sessions stopped.", updatedRows);
            }
            else {
                Timber.d("No opened session. Start new session.");
                Session session = new Session(0, System.currentTimeMillis() / 1000L, 0);
                if (mGroupsModel.appendSession(session) > 0) {
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
                new SessionSavedEvent(mGroupsModel.updateSession(session) > 0)));
    }

    public LiveData<Session> getSessionById(long id) {
        return mGroupsModel.getSessionById(id);
    }

    @Override
    public LiveData<List<Long>> getSessionsIds() {
        return mGroupsModel.getSessionsIds();
    }

    @Override
    public Flowable<DayDescription> getDayDescriptionRx(Long date) {
        return mGroupsModel.getDayDescriptionRx(date);
    }

    @Override
    public void updateDayDescription(DayDescription dayDescription) {
        mExecutors.getDiskIO().execute(() -> {
            boolean wasSaved = mGroupsModel.updateDayDescription(dayDescription) != 0;
            if (!wasSaved)
                wasSaved = mGroupsModel.appendDayDescription(dayDescription) > 0;
            EventBus.getDefault().post(new DayDescriptionSavedEvent(wasSaved));
        });
    }

    public LiveData<List<DayGroup>> getDayGroups(List<Long> days) {
        return new ComputableData<List<DayGroup>>(mExecutors.getMainThread(), mExecutors.getDiskIO()) {
            private InvalidationTracker.Observer mObserver;

            @Override
            protected List<DayGroup> compute() {
                if (mObserver == null) {
                    mObserver = new InvalidationTracker.Observer("sessions", "days") {
                        @Override
                        public void onInvalidated(@NonNull Set<String> tables) {
                            invalidate();
                        }
                    };
                }
                mDatabase.getInvalidationTracker().addObserver(mObserver);
                return mDatabase.groupsModel().getDayGroups(days);
            }
        };
    }

    @Override
    public Flowable<List<DayGroup>> getDayGroupsRx(List<Long> days) {
        return mGroupsModel.getDayGroupsRx(days);
    }

    @Override
    public void appendAll(ArrayList<Session> list) {
        mExecutors.getDiskIO().execute(() -> mGroupsModel.appendAll(list));
    }

    public AppDatabase getDatabase() {
        return mDatabase;
    }
}
