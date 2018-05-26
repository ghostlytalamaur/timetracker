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
import mvasoft.timetracker.data.event.SessionSavedEvent;
import mvasoft.timetracker.data.event.SessionToggledEvent;
import mvasoft.timetracker.data.event.SessionsDeletedEvent;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.SessionsDao;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

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
        mExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(
                        new SessionsDeletedEvent(mDatabase.groupsModel().deleteByIds(ids)));
            }
        });
    }

    @Override
    public LiveData<Long> getOpenedSessionId() {
        return mGroupsModel.getOpenedSessionId();
    }

    @Override
    public void toggleSession() {
        mExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                int updatedRows = mGroupsModel.closeOpenedSessions();
                ToggleSessionResult res;
                if (updatedRows != 0)
                    res = ToggleSessionResult.tgs_Stopped;
                else {
                    Session session = new Session(0, System.currentTimeMillis() / 1000L, 0);
                    if (mGroupsModel.appendSession(session) > 0)
                        res = ToggleSessionResult.tgs_Started;
                    else
                        res = ToggleSessionResult.tgs_Error;
                }

                EventBus.getDefault().post(new SessionToggledEvent(res));
            }
        });

    }

    @Override
    public void updateSession(Session session) {
        mExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(
                        new SessionSavedEvent(mGroupsModel.updateSession(session) > 0));
            }
        });
    }

    public LiveData<Session> getSessionById(long id) {
        return mGroupsModel.getSessionById(id);
    }

    @Override
    public Flowable<Session> getSessionByIdRx(long id) {
        return mGroupsModel.getSessionByIdRx(id);
    }

    @Override
    public LiveData<List<Long>> getSessionsIds() {
        return mGroupsModel.getSessionsIds();
    }

    @Override
    public LiveData<DayDescription> getDayDescription(Long date) {
        return mGroupsModel.getDayDescription(date);
    }

    @Override
    public void updateDayDescription(DayDescription dayDescription) {
        mExecutors.getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                int cnt = mGroupsModel.updateDayDescription(dayDescription);
                if (cnt == 0)
                    mGroupsModel.appendDayDescription(dayDescription);
            }
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
    public void appendAll(ArrayList<Session> list) {
        mExecutors.getDiskIO().execute(() -> mGroupsModel.appendAll(list));
    }

    //    @Override
    public LiveData<List<Session>> getSessionForDate(long date) {
        return mGroupsModel.getSessionForDate(date);
    }

    public AppDatabase getDatabase() {
        return mDatabase;
    }
}
