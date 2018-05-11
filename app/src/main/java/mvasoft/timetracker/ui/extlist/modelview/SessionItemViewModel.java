package mvasoft.timetracker.ui.extlist.modelview;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.databinding.recyclerview.BaseItemModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.TimeInfoProvider;

public class SessionItemViewModel extends BaseObservable implements BaseItemModel {

    private boolean mIsSelected;
    private final DateTimeFormatters mFormatter;
    private final TimeInfoProvider mTimeInfo;

    SessionItemViewModel(DateTimeFormatters formatter, TimeInfoProvider timeInfo) {
        mFormatter = formatter;
        mTimeInfo = timeInfo;
    }

    public String getStartTime() {
        if (mTimeInfo != null)
            return /*mFormatter.formatDate(mTimeInfo.getStartTime()) + " " +*/ mFormatter.formatTime(mTimeInfo.getStartTime());
        else
            return "Start date";
    }

    public String getEndTime() {
         if (mTimeInfo != null)
             return /*mFormatter.formatDate(mTimeInfo.getEndTime()) + " " +*/ mFormatter.formatTime(mTimeInfo.getEndTime());
        else
            return "End date";
    }

    public String getDuration() {
        if (mTimeInfo != null)
            return mFormatter.formatDuration(mTimeInfo.getDuration());
        else
            return "Duration";
    }

    public boolean getIsRunning() {
        return (mTimeInfo != null) && mTimeInfo.isRunning();
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
        if (mTimeInfo != null)
            return mTimeInfo.getId();
        else
            return 0;
    }

    public String asString() {
        return String.format("%s - %s: %s\n",
                mFormatter.formatDate(mTimeInfo.getStartTime()),
                mFormatter.formatDate(mTimeInfo.getEndTime()),
                mFormatter.formatDuration(mTimeInfo.getDuration()));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SessionItemViewModel))
            return false;
        SessionItemViewModel to = (SessionItemViewModel) obj;
        return getIsSelected() == to.getIsSelected() &&
                (mTimeInfo != null && mTimeInfo.equals(to.mTimeInfo));
    }
}
