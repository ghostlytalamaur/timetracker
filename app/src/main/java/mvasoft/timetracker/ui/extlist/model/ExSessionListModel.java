package mvasoft.timetracker.ui.extlist.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import org.joda.time.DateTime;

import java.util.List;

import dagger.Lazy;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.vo.Session;

public class ExSessionListModel {

    private static final long TARGET_TIME = 8 * 60 * 60;
    private final MutableLiveData<Long> mDateLiveData;
    private final Lazy<AppPreferences> mPreferences;
    private LiveData<List<Session>> mSessions;
    private Lazy<DataRepository> mRepository;

    public ExSessionListModel(Lazy<DataRepository> repository, Lazy<AppPreferences> preferences) {
        mRepository = repository;
        mPreferences = preferences;
        mDateLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Session>> getSessionList() {
        if (mSessions == null)
            mSessions = Transformations.switchMap(mDateLiveData,
                    (date) -> mRepository.get().getSessionForDate(date));
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

    public void setDate(long date) {
        if (mDateLiveData.getValue() == null || mDateLiveData.getValue() != date)
            mDateLiveData.setValue(date);
    }

    public long getSummaryTime() {
        List<Session> list = getSessionList().getValue();
        long summary = 0;
        if (list != null)
            for (Session s : list)
                summary += s.getDuration();
        return summary;
    }

    private long getTargetTime() {
        if (isWorkingDay())
            return mPreferences.get().getTargetTime();
        else
            return 0;
    }

    private boolean isWorkingDay() {
        // TODO: cache value
        return mDateLiveData.getValue() != null && mPreferences.get().isWorkingDay(new DateTime(mDateLiveData.getValue() * 1000).getDayOfWeek());
    }

    public long getTargetDiff() {
        return getSummaryTime() - getTargetTime();
    }
}
