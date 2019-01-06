package mvasoft.timetracker.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mvasoft.timetracker.utils.DateTimeHelper;

public class SessionUtils {
    private SessionUtils() {}


    public static long getDuration(List<Session> sessions) {
        long duration = 0;
        if (sessions != null) {
            for (Session s : sessions) {
                duration += s.getDuration();
            }
        }
        return duration;
    }

    public static void sortGroups(List<SessionsGroup> groups) {
        if (groups == null)
            return;

        Collections.sort(groups, (l, r) -> {
            int res = Long.compare(l.getStart(), r.getStart());
            if (res == 0)
                res = Long.compare(l.getEnd(), r.getEnd());
            return res;
        });
    }

    public static void sortSessions(List<Session> sessions) {
        if (sessions == null)
            return;

        Collections.sort(sessions, (l, r) -> {
            int res = Long.compare(l.getStartTime(), r.getStartTime());
            if (res == 0)
                res = Long.compare(l.getEndTime(), r.getEndTime());
            return res;
        });
    }

    public static List<Session> getSessionsForDay(Iterable<Session> sessions, long unixTime) {
        if (sessions == null)
            return null;
        List<Session> result = null;
        for (Session s : sessions) {
            if (DateTimeHelper.sameDays(unixTime, s.getStartTime())) {
                if (result == null)
                    result = new ArrayList<>();
                result.add(s);
            }
        }
        return result;
    }
}
