package mvasoft.timetracker.data.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.SessionsDao;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;
import mvasoft.timetracker.vo.SessionWithDescription;

@Singleton
public class RoomDataRepositoryImpl implements DataRepository {

    private final SessionsDao mGroupsModel;
    private final AppDatabase mDatabase;
    private final Executor mExecutor;

    @Inject
    RoomDataRepositoryImpl(Application application) {
        mDatabase = AppDatabase.getDatabase(application);
        mGroupsModel = mDatabase.groupsModel();
        mExecutor = Executors.newSingleThreadExecutor();

    }

    @Override
    public LiveData<List<Session>> getSessions() {
        return mGroupsModel.getAll();
    }

    @Override
    public LiveData<Integer> deleteSessions(List<Long> ids) {
        MutableLiveData<Integer> resData = new MutableLiveData<>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                resData.postValue(mDatabase.groupsModel().deleteByIds(ids));
            }
        });
        return resData;
    }

    @Override
    public LiveData<Long> getOpenedSessionId() {
        return mGroupsModel.getOpenedSessionId();
    }

    @Override
    public LiveData<ToggleSessionResult> toggleSession() {
        MutableLiveData<ToggleSessionResult> resData = new MutableLiveData<>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int updatedRows = mGroupsModel.closeOpenedSessions();
                if (updatedRows != 0)
                    resData.postValue(ToggleSessionResult.tgs_Stopped);
                else {
                    Session session = new Session(0, System.currentTimeMillis() / 1000L, 0);
                    if (mGroupsModel.appendSession(session) > 0)
                        resData.postValue(ToggleSessionResult.tgs_Started);
                    else
                        resData.postValue(ToggleSessionResult.tgs_Error);
                }

            }
        });

        return resData;
    }

    @Override
    public LiveData<Boolean> updateSession(Session session) {
        MutableLiveData<Boolean> resData = new MutableLiveData<>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                resData.postValue(mGroupsModel.updateSession(session) > 0);
            }
        });

        return resData;
    }

    public LiveData<Session> getSessionById(long id) {
        return mGroupsModel.getSessionById(id);
    }

    @Override
    public LiveData<List<SessionWithDescription>> getSessionForDate(long date) {
        long startDate = date - 7 * 24 * 60 * 60;
        long endDate = date + 7 * 24 * 60 * 60;
//        LiveData<List<SessionWithDescription>> res = mGroupsModel.getSessionWithDescription(startDate, endDate);
//        return Transformations.map(res, new Function<List<SessionWithDescription>, List<Session>>() {
//            @Override
//            public List<Session> apply(List<SessionWithDescription> input) {
//                List<Session> out = new ArrayList<>(input);
//                return out;
//            }
//        });
        return mGroupsModel.getSessionForDate(date);
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
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int cnt = mGroupsModel.updateDayDescription(dayDescription);
                if (cnt == 0)
                    mGroupsModel.appendDayDescription(dayDescription);
            }
        });
    }

}
