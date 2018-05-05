package mvasoft.timetracker.ui.extlist.modelview;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.databinding.recyclerview.BaseItemModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.Session;

public class SessionItemViewModel extends BaseObservable implements BaseItemModel {

    private boolean mIsSelected;
    private final DateTimeFormatters mFormatter;
    private final Session mSession;

    SessionItemViewModel(DateTimeFormatters formatter, Session session) {
        mFormatter = formatter;
        mSession = session;
    }

    public String getStartDate() {
        if (mSession != null)
            return "id = " + getId() + mFormatter.formatDate(mSession.getStartTime());
        else
            return "Start date";
    }

    public String getEndDate() {
         if (mSession != null)
            return mFormatter.formatDate(mSession.getEndTime());
        else
            return "End date";
    }

    public String getStartTime() {
        if (mSession != null)
            return mFormatter.formatTime(mSession.getStartTime());
        else
            return "Start date";
    }

    public String getEndTime() {
         if (mSession != null)
             return mFormatter.formatTime(mSession.getEndTime());
        else
            return "End date";
    }

    public String getDuration() {
        if (mSession != null)
            return mFormatter.formatPeriod(mSession.getDuration());
        else
            return "Duration";
    }

    public String getGoalTimeDiff() {
        if (mSession != null) {
            return mFormatter.formatPeriod(mSession.getGoalTimeDiff());
        }
        else
            return "Target";
    }

    public boolean getIsGoalAchieved() {
        return mSession != null && mSession.isGoalAchieved();
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
//        if (mSession != null)
//            mSession.deleteObserver(mObserver);
    }

    @Override
    public void dataChanged() {
        notifyChange();
    }

    @Override
    public long getId() {
        if (mSession != null)
            return mSession.getId();
        else
            return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SessionItemViewModel))
            return false;
        SessionItemViewModel to = (SessionItemViewModel) obj;
        return getIsSelected() == to.getIsSelected() &&
                (mSession != null && mSession.equals(to.mSession));
    }
}
