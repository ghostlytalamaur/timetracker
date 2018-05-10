package mvasoft.timetracker.utils;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.List;

public class DateTimeHelper {

    private DateTimeHelper() {}


    public static boolean sameDays(long first, long second) {
        return Days.daysBetween(new DateTime(first * 1000),
                new DateTime(second * 1000)).getDays() == 0;
    }


    public static int daysBetween(long unixStart, long unixEnd) {
        return Days.daysBetween(new DateTime(unixStart * 1000), new DateTime(unixEnd * 1000)).getDays();
    }

    public static List<Long> daysList(long unixStart, long unixEnd) {
        if (unixEnd < unixStart)
            return null;

        DateTime start = new DateTime(unixStart * 1000).withTimeAtStartOfDay();
        DateTime end = new DateTime(unixEnd * 1000).withTimeAtStartOfDay().plusHours(23).plusMinutes(29);
        int cnt = Days.daysBetween(start, end).getDays() + 1;
        ArrayList<Long> res = new ArrayList<>(cnt);
        for (int i = 0; i < cnt; i++)
            res.add(start.plusDays(i).getMillis() / 1000);
        return res;
    }

    public static long startOfWeek(long unixSec) {
        DateTime dt = new DateTime(unixSec * 1000);
        return dt.dayOfWeek().withMinimumValue().getMillis() / 1000;
    }

    public static long endOfWeek(long unixSec) {
        DateTime dt = new DateTime(unixSec * 1000);
        return dt.dayOfWeek().withMaximumValue().getMillis() / 1000;
    }

    public static long startOfMonth(long unixSec) {
        DateTime dt = new DateTime(unixSec * 1000);
        return dt.dayOfMonth().withMinimumValue().getMillis() / 1000;
    }

    public static long endOfMonth(long unixSec) {
        DateTime dt = new DateTime(unixSec * 1000);
        return dt.dayOfMonth().withMaximumValue().getMillis() / 1000;
    }

    public static int dayOfWeek(Long unixSec) {
        return new DateTime(unixSec * 1000).dayOfWeek().get();
    }
}