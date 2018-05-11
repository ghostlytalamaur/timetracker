package mvasoft.timetracker.ui.extlist.modelview;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.databinding.recyclerview.BaseItemModel;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.DayGroup;

public class DayItemViewModel extends BaseObservable implements BaseItemModel {

    private boolean mIsSelected;
    private final DateTimeFormatters mFormatter;
    private final DayGroup mDayGroup;
    private final AppPreferences mPreferences;

    DayItemViewModel(DateTimeFormatters formatter, DayGroup dayGroup, AppPreferences preferences) {
        mFormatter = formatter;
        mDayGroup = dayGroup;
        mPreferences = preferences;
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

    public String getDuration() {
        if (mDayGroup != null)
            return mFormatter.formatDuration(mDayGroup.getDuration());
        else
            return "Duration";
    }

    public String getTargetTimeDiff() {
        if (mDayGroup != null)
            return mFormatter.formatDuration(mDayGroup.getTargetTimeDiff(mPreferences));
        else
            return "Target";
    }

    public boolean getIsTargetAchieved() {
        return mDayGroup != null && mDayGroup.getTargetTime(mPreferences) >= 0;
    }

    public boolean getIsRunning() {
        return (mDayGroup != null) && mDayGroup.isRunning();
    }

    @Bindable
    @Override
    public boolean getIsSelected() {
        return mIsSelected;
    }

    @Override
    public void setIsSelected(boolean selected) {
        mIsSelected = selected;
        notifyPropertyChanged(BR.isSelected);
    }

    @Override
    public void onCleared() {
    }

    @Override
    public void dataChanged() {
        notifyChange();
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
    public boolean equals(Object obj) {
        if (!(obj instanceof DayItemViewModel))
            return false;
        DayItemViewModel to = (DayItemViewModel) obj;
        return getIsSelected() == to.getIsSelected() &&
                (mDayGroup != null && mDayGroup.equals(to.mDayGroup));
    }
}
