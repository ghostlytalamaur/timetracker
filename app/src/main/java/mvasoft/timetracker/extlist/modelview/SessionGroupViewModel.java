package mvasoft.timetracker.extlist.modelview;

import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.extlist.model.BaseItemModel;
import mvasoft.timetracker.ui.DateTimeFormatters;

public class SessionGroupViewModel implements BaseItemModel {

    private final DateTimeFormatters mFormatter;
    private final GroupsList.SessionGroup mGroup;

    public SessionGroupViewModel(DateTimeFormatters formatter, GroupsList.SessionGroup group) {
        mFormatter = formatter;
        mGroup = group;
    }

    private boolean isStarted() {
        return (mGroup != null) && mGroup.isRunning();
    }

    public String getStartDate() {
        if (mGroup != null)
            return mFormatter.formatDate(mGroup.getStart());
        else
            return "Start date";
    }

    public String getEndDate() {
        if (!isStarted())
            return mFormatter.formatDate(mGroup.getEnd());
        else if (mGroup != null)
            return "";
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
        if (!isStarted())
            return mFormatter.formatTime(mGroup.getEnd());
        else if (mGroup != null)
            return "";
        else
            return "End date";
    }

    public String getDuration() {
        if (!isStarted())
            return mFormatter.formatPeriod(mGroup.getDuration());
        else if (mGroup != null)
            return "";
        else
            return "Duration";
    }

    @Override
    public long getId() {
        if (mGroup != null)
            return mGroup.getID();
        else
            return 0;
    }
}
