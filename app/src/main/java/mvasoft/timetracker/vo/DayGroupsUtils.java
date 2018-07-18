package mvasoft.timetracker.vo;

import java.util.Collections;
import java.util.List;

import mvasoft.timetracker.utils.DateTimeHelper;

public class DayGroupsUtils {
    private DayGroupsUtils() {}

    public static DayGroup getDayGroupForDay(List<DayGroup> sortedGroups, long unixSec) {
        int idx = -1;
        if (sortedGroups != null) {
            idx = Collections.binarySearch(sortedGroups,
                    new DayGroup(unixSec, null, null),
                    (o1, o2) -> {
                        if (o1 == o2)
                            return 0;
                        else
                            return DateTimeHelper.compareDays(o1.getDay(), o2.getDay());
                    });
        }

        return idx >= 0 ? sortedGroups.get(idx) : null;
    }


    public static long getDuration(List<DayGroup> groups) {
        long duration = 0;
        if (groups != null) {
            for (DayGroup group : groups) {
                duration += group.getDuration();
            }
        }
        return duration;
    }

    public static void sortGroups(List<DayGroup> groups) {
        if (groups == null)
            return;

        Collections.sort(groups, (g1, g2) -> {
            if (g1 == g2)
                return 0;
            else
                return Long.compare(g1.getStartTime(), g2.getStartTime());
        });
    }
}
