package mvasoft.timetracker.data.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.deprecated.SessionHelper;
import mvasoft.timetracker.vo.Session;

@Singleton
public class RoomDataRepositoryImpl implements DataRepository {

    private final GroupsListDao mGroupsModel;
    private final AppDatabase mDatabase;

    @Inject
    RoomDataRepositoryImpl(Application application) {
        mDatabase = AppDatabase.getDatabase(application);
        mGroupsModel = mDatabase.groupsModel();

    }

    @Override
    public LiveData<GroupsList> getGroups(GroupType groupType) {
        LiveData<List<Session>> data = null;
        switch (groupType) {
            case gt_None:
                data = mGroupsModel.getAll();
                break;
            case gt_Day:
                data = mGroupsModel.getAll();
                break;
            case gt_Week:
                data = mGroupsModel.getAll();
                break;
            case gt_Month:
                data = mGroupsModel.getAll();
                break;
            case gt_Year:
                data = mGroupsModel.getAll();
                break;
        }

        return EntityDaoWrappers.wrapEntityList(data);
    }

    @Override
    public LiveData<Integer> deleteGroups(GroupType groupType, List<Long> groupIds) {
        MutableLiveData<Integer> data = new MutableLiveData<>();
        new DeleteSessionsAsync(mDatabase, data, groupType, groupIds).execute();
        return data;
    }

    @Override
    public LiveData<Long> getOpenedSessionId() {
        return mGroupsModel.getOpenedSessionId();
    }

    @Override
    public MutableLiveData<SessionHelper.ToggleSessionResult> toggleSession() {
        MutableLiveData<SessionHelper.ToggleSessionResult> res = new MutableLiveData<>();
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
        private final MutableLiveData<SessionHelper.ToggleSessionResult> mLiveData;

        ToggleSessionAsync(AppDatabase db, MutableLiveData<SessionHelper.ToggleSessionResult> resData) {
            mDb = db;
            mLiveData = resData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mDb.groupsModel().closeOpenedSessions() != 0)
                mLiveData.postValue(SessionHelper.ToggleSessionResult.tgs_Stopped);
            else if (mDb.groupsModel().appendSession(new Session(0, System.currentTimeMillis() / 1000L, 0)) > 0)
                mLiveData.postValue(SessionHelper.ToggleSessionResult.tgs_Started);
            else
                mLiveData.postValue(SessionHelper.ToggleSessionResult.tgs_Stopped);

            return null;
        }
    }

    private static class DeleteSessionsAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        private final MutableLiveData<Integer> mLiveData;
        private final List<Long> mGroupIds;
        private final GroupType mGroupType;

        DeleteSessionsAsync(AppDatabase db, MutableLiveData<Integer> resData,
                            GroupType groupType, List<Long> groupIds) {
            mDb = db;
            mLiveData = resData;
            mGroupType = groupType;
            mGroupIds = groupIds;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            int cnt = mDb.groupsModel().deleteByIds(mGroupIds);
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
