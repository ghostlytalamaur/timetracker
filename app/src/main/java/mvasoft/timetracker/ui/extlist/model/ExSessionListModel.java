package mvasoft.timetracker.ui.extlist.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

import dagger.Lazy;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;
import mvasoft.timetracker.vo.SessionWithDescription;

public class ExSessionListModel {

    private final MutableLiveData<Long> mDateLiveData;
    private final Lazy<AppPreferences> mPreferences;
    private LiveData<List<SessionWithDescription>> mSessions;
    private LiveData<DayDescription> mDayDescription;
    private Lazy<DataRepository> mRepository;

    private LiveData<List<DayGroup>> mDayGroups;

    public ExSessionListModel(Lazy<DataRepository> repository, Lazy<AppPreferences> preferences) {
        mRepository = repository;
        mPreferences = preferences;
        mDateLiveData = new MutableLiveData<>();

        mSessions = Transformations.switchMap(mDateLiveData,
                (date) -> mRepository.get().getSessionForDate(date));

        mDayDescription = new MutableLiveData<>();
        mDayDescription = Transformations.switchMap(mDateLiveData,
                (date) -> mRepository.get().getDayDescription(date));

        // Empty observer to compute mDayDescription value every time, when mDateLiveData changes
        // LiveData does not update their value when no active observers
        // TODO: think about leaks
        mDayDescription.observeForever(dayDescription -> {});
        mDayGroups = Transformations.switchMap(mDateLiveData,
                (date) -> mRepository.get().getDayGroups(Arrays.asList(date)));
        mDayGroups.observeForever(new Observer<List<DayGroup>>() {
            @Override
            public void onChanged(@Nullable List<DayGroup> dayGroups) {

            }
        });
    }

    public LiveData<List<SessionWithDescription>> getSessionList() {
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
        List<? extends Session> list = getSessionList().getValue();
        long summary = 0;
        if (list != null)
            for (Session s : list)
                summary += s.getDuration();
        return summary;
    }

    private long getTargetTime() {
        if (mDayDescription.getValue() == null)
            return mPreferences.get().getTargetTimeInMin() * 60;
        else if (isWorkingDay())
            return mDayDescription.getValue().getTargetDuration() * 60;
        else
            return 0;
    }

    private boolean isWorkingDay() {
        // TODO: cache value
        if (mDayDescription.getValue() != null)
            return mDayDescription.getValue().isWorkingDay();
        else
            return mDateLiveData.getValue() != null &&
                    mPreferences.get().isWorkingDay(new DateTime(mDateLiveData.getValue() * 1000).getDayOfWeek());
    }

    public long getTargetDiff() {
        return getSummaryTime() - getTargetTime();
    }
}
