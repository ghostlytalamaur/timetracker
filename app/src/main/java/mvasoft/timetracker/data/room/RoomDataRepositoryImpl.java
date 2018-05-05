package mvasoft.timetracker.data.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.SessionsDao;
import mvasoft.timetracker.vo.Session;

@Singleton
public class RoomDataRepositoryImpl implements DataRepository {

    private final SessionsDao mGroupsModel;
    private final AppDatabase mDatabase;

    @Inject
    RoomDataRepositoryImpl(Application application) {
        mDatabase = AppDatabase.getDatabase(application);
        mGroupsModel = mDatabase.groupsModel();

    }

    @Override
    public LiveData<List<Session>> getSessions() {
        return mGroupsModel.getAll();
    }

    @Override
    public LiveData<Integer> deleteSessions(List<Long> ids) {
        MutableLiveData<Integer> data = new MutableLiveData<>();
        new DeleteSessionsAsync(mDatabase, data, ids).execute();
        return data;
    }

    @Override
    public LiveData<Long> getOpenedSessionId() {
        return mGroupsModel.getOpenedSessionId();
    }

    @Override
    public MutableLiveData<ToggleSessionResult> toggleSession() {
        MutableLiveData<ToggleSessionResult> res = new MutableLiveData<>();
        new ToggleSessionAsync(mDatabase, res).execute();
        return res;
    }

    @Override
    public LiveData<Boolean> updateSession(Session session) {
        MutableLiveData<Boolean> data = new MutableLiveData<>();
        new UpdateSessionAsync(mDatabase, session, data).execute();
        return data;
    }

    public LiveData<Session> getSessionById(long id) {
        return mGroupsModel.getSessionById(id);
    }

    private static class ToggleSessionAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        private final MutableLiveData<ToggleSessionResult> mLiveData;

        ToggleSessionAsync(AppDatabase db, MutableLiveData<ToggleSessionResult> resData) {
            mDb = db;
            mLiveData = resData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mDb.groupsModel().closeOpenedSessions() != 0)
                mLiveData.postValue(ToggleSessionResult.tgs_Stopped);
            else if (mDb.groupsModel().appendSession(new Session(0, System.currentTimeMillis() / 1000L, 0)) > 0)
                mLiveData.postValue(ToggleSessionResult.tgs_Started);
            else
                mLiveData.postValue(ToggleSessionResult.tgs_Stopped);

            return null;
        }
    }

    private static class DeleteSessionsAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        private final MutableLiveData<Integer> mLiveData;
        private final List<Long> mIds;

        DeleteSessionsAsync(AppDatabase db, MutableLiveData<Integer> resData, List<Long> ids) {
            mDb = db;
            mLiveData = resData;
            mIds = ids;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            int cnt = mDb.groupsModel().deleteByIds(mIds);
            mLiveData.postValue(cnt);
            return null;
        }
    }

    private static class UpdateSessionAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        private final Session mSession;
        private final MutableLiveData<Boolean> mLiveData;


        private UpdateSessionAsync(AppDatabase db, Session session, MutableLiveData<Boolean> liveData) {
            mDb = db;
            mSession = session;
            mLiveData = liveData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mLiveData.postValue(mDb.groupsModel().updateSession(mSession) > 0);
            return null;
        }
    }
}
