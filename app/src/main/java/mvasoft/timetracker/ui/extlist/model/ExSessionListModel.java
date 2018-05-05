package mvasoft.timetracker.ui.extlist.model;

import android.arch.lifecycle.LiveData;

import java.util.List;

import dagger.Lazy;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.vo.Session;

public class ExSessionListModel {

    private LiveData<List<Session>> mSessions;
    private Lazy<DataRepository> mRepository;

    public ExSessionListModel(Lazy<DataRepository> repository) {
        mRepository = repository;
    }

    public LiveData<List<Session>> getSessionList() {
        if (mSessions == null)
            mSessions = mRepository.get().getSessions();
        return mSessions;
    }

    public boolean hasOpenedSessions() {
        if (getSessionList().getValue() != null)
            for (Session s : getSessionList().getValue())
                if (s.getEndTime() == 0)
                    return true;

        return false;
    }

    public Session getById(long id) {
        if (getSessionList().getValue() != null)
            for (Session s : getSessionList().getValue())
                if (s.getId() == id)
                    return s;

        return null;
    }
}
