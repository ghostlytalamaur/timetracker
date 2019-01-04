package mvasoft.timetracker.vo;

import java.util.List;

import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.utils.CollectionsUtils;

public class SessionsGroup {

    private final boolean mHasOpenedSessions;
    private final List<Session> mList;

    public SessionsGroup(List<Session> list) {
        mList = list;

        boolean hasOpened = false;
        for (Session s : mList) {
            if (s.isRunning()) {
                hasOpened = true;
                break;
            }
        }
        mHasOpenedSessions = hasOpened;
    }

    public long getId() {
        if (mList.size() > 0)
            return mList.get(0).getId();
        return -1;
    }

    public void collectIds(List<Long> destList) {
        for (Session s : mList)
            destList.add(s.getId());
    }

    public long getStart() {
        long start = 0;
        for (Session s : mList)
            if (start == 0 || start > s.getStartTime())
                start = s.getStartTime();
        return start;
    }

    public long getEnd() {
        long end = 0;
        for (Session s : mList)
            if (end < s.getEndTime())
                end = s.getEndTime();
        return end;

    }

    public boolean hasOpenedSessions() {
        return mHasOpenedSessions;
    }

    public long calculateDuration() {
        long duration = 0;
        for (Session s : mList)
            duration += s.getDuration();
        return duration;
    }

    public int sessionsCount() {
        return mList.size();
    }

    public enum GroupType {
        gtNone,
        gtDay,
        gtWeek,
        gtMonth,
        gtYear
    }

    public static CollectionsUtils.Function<Session, Long> getGroupFunction(GroupType groupType) {
        switch (groupType) {
            case gtNone: return Session::getId;
            case gtDay: return (s) -> DateTimeHelper.startOfDay(s.getStartTime());
            case gtWeek: return (s) -> DateTimeHelper.startOfWeek(s.getStartTime());
            case gtMonth: return (s) -> DateTimeHelper.startOfMonth(s.getStartTime());
            case gtYear: return (s) -> DateTimeHelper.startOfMonth(s.getStartTime());
            default: return (s) -> 0L;
        }
    }
}
