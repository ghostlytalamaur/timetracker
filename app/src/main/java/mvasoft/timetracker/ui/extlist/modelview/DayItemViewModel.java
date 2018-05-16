package mvasoft.timetracker.ui.extlist.modelview;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

public class DayItemViewModel extends BaseItemViewModel {

    private final DateTimeFormatters mFormatter;
    private final DayGroup mDayGroup;
    private final AppPreferences mPreferences;
    private final MutableLiveData<String> mDurationLiveData;

    DayItemViewModel(DateTimeFormatters formatter, DayGroup dayGroup, AppPreferences preferences) {
        mFormatter = formatter;
        mDayGroup = dayGroup;
        mPreferences = preferences;
        mDurationLiveData = new MutableLiveData<>();
        updateDuration();
    }

    public String getStartTime() {
        if (mDayGroup != null)
            return mFormatter.formatDate(mDayGroup.getStartTime()) + " " + mFormatter.formatTime(mDayGroup.getStartTime());
        else
            return "Start date";
    }

    public String getEndTime() {
         if (mDayGroup != null)
             return mFormatter.formatDate(mDayGroup.getEndTime()) + " " + mFormatter.formatTime(mDayGroup.getEndTime());
        else
            return "End date";
    }

    public LiveData<String> getDuration() {
        return mDurationLiveData;
    }

    @Override
    void updateDuration() {
        if (mDayGroup != null)
            mDurationLiveData.postValue(mFormatter.formatDuration(mDayGroup.getDuration()));
    }

    public String getTargetTimeDiff() {
        if (mDayGroup != null)
            return mFormatter.formatDuration(mDayGroup.getTargetTimeDiff(mPreferences));
        else
            return "Target";
    }

    public boolean getIsTargetAchieved() {
        return mDayGroup != null && mDayGroup.getTargetTimeDiff(mPreferences) >= 0;
    }

    @Override
    public boolean getIsRunning() {
        return (mDayGroup != null) && mDayGroup.isRunning();
    }


    @Override
    public void dataChanged() {
//        notifyChange();
    }

    @Override
    public long getId() {
        if (mDayGroup != null)
            return mDayGroup.getId();
        else
            return 0;
    }

    public String asString() {
        return String.format("%s - %s: %s\n",
                mFormatter.formatDate(mDayGroup.getStartTime()),
                mFormatter.formatDate(mDayGroup.getEndTime()),
                mFormatter.formatDuration(mDayGroup.getDuration()));
    }

    @Override
    void appendSessionIds(@NonNull List<Long> destList) {
        if (mDayGroup != null && mDayGroup.getSessions() != null)
            for (Session s : mDayGroup.getSessions())
                destList.add(s.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DayItemViewModel))
            return false;
        DayItemViewModel to = (DayItemViewModel) obj;
        return getIsSelected() == to.getIsSelected() &&
                (mDayGroup != null && mDayGroup.equals(to.mDayGroup));
    }
}
