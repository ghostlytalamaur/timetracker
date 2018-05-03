package mvasoft.timetracker.ui.extlist.modelview;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.Observable;
import java.util.Observer;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.databinding.recyclerview.BaseItemModel;
import mvasoft.timetracker.utils.DateTimeFormatters;

public class SessionGroupViewModel extends BaseObservable implements BaseItemModel {

    private boolean mIsSelected;
    private final DateTimeFormatters mFormatter;
    private final GroupsList.SessionGroup mGroup;
    private Observer mObserver;

    SessionGroupViewModel(DateTimeFormatters formatter, GroupsList.SessionGroup group) {
        mFormatter = formatter;
        mGroup = group;
        mObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                notifyChange();
            }
        };

        mGroup.addObserver(mObserver);
    }

    private boolean isStarted() {
        return (mGroup != null) && mGroup.isRunning();
    }

    public String getStartDate() {
        if (mGroup != null)
            return "id = " + getId() + mFormatter.formatDate(mGroup.getStart());
        else
            return "Start date";
    }

    public String getEndDate() {
         if (mGroup != null)
            return mFormatter.formatDate(mGroup.getEnd());
        else
            return "End date";
    }

    public String getStartTime() {
        if (mGroup != null)
            return mFormatter.formatTime(mGroup.getStart());
        else
            return "Start date";
    }

    public String getEndTime() {
         if (mGroup != null)
             return mFormatter.formatTime(mGroup.getEnd());
        else
            return "End date";
    }

    public String getDuration() {
        if (mGroup != null)
            return mFormatter.formatPeriod(mGroup.getDuration());
        else
            return "Duration";
    }

    public String getGoalTimeDiff() {
        if (mGroup != null) {
            long target = mGroup.getGoalTimeDiff();
            return String.format(target > 0 ? "+%s" : "-%s", mFormatter.formatPeriod(Math.abs(target)));
        }
        else
            return "Target";
    }

    public boolean getIsGoalAchieved() {
        return mGroup != null && mGroup.isGoalAchieved();
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
        if (mGroup != null)
            mGroup.deleteObserver(mObserver);
    }

    @Override
    public long getId() {
        if (mGroup != null)
            return mGroup.getID();
        else
            return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SessionGroupViewModel))
            return false;
        SessionGroupViewModel to = (SessionGroupViewModel) obj;
        return getIsSelected() == to.getIsSelected() &&
                (mGroup.getID() == to.mGroup.getID()) &&
                (mGroup.getStart() == to.mGroup.getStart()) &&
                (mGroup.getEnd() == to.mGroup.getEnd());
    }
}
