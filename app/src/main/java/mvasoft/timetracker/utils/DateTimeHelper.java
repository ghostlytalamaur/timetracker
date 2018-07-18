package mvasoft.timetracker.utils;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.List;

public class DateTimeHelper {

    private DateTimeHelper() {}

    public static int compareDays(long first, long second) {
        return Long.compare(DateTimeHelper.startOfDay(first),
                DateTimeHelper.startOfDay(second));
    }

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

    /**
     * calculate start of "monthly" week.
     * monthly week always started at first day of month, not at monday
     *
     * @param unixSec second since unix epoch
     * @return unix seconds according to startOfMonthWeek
     */
    public static long startOfMonthWeek(long unixSec) {
        DateTime res = new DateTime(unixSec * 1000).dayOfWeek().withMinimumValue();
        DateTime monthStart = new DateTime(unixSec * 1000).dayOfMonth().withMinimumValue();
        if (res.getWeekOfWeekyear() == monthStart.getWeekOfWeekyear()) {
            return monthStart.getMillis() / 1000;
        }
        else
            return res.getMillis() / 1000;
    }


    /**
     * calculate end of "monthly" week.
     * monthly week always ended at last day of month, not at sunday
     *
     * @param unixSec second since unix epoch
     * @return unix seconds according to startOfMonthWeek
     */
    public static long endOfMonthWeek(long unixSec) {
        DateTime res = new DateTime(unixSec * 1000).dayOfWeek().withMaximumValue();
        DateTime monthEnd = new DateTime(unixSec * 1000).dayOfMonth().withMaximumValue();
        if (res.getWeekOfWeekyear() == monthEnd.getWeekOfWeekyear()) {
            return monthEnd.getMillis() / 1000;
        }
        else
            return res.getMillis() / 1000;
    }

    public static long startOfDay(long unixSec) {
        DateTime dt = new DateTime(unixSec * 1000);
        return dt.withTimeAtStartOfDay().getMillis() / 1000;

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

    public static long getUnixTime(int year, int month, int dayOfMonth) {
        DateTime dt = new DateTime(year, month, dayOfMonth, 0, 0, 0);
        return dt.getMillis() / 1000;
    }

    public static long plusDay(long unixSec, int days) {
        DateTime dt = new DateTime(unixSec * 1000);
        return dt.plusDays(days).getMillis() / 1000;
    }

    public static long roundDateTime(long unixSec, long roundToMin) {
        if (roundToMin > 0) {
            long roundToSec = roundToMin * 60;
            long cnt = unixSec / roundToSec;
            if ((unixSec % roundToSec) >= (roundToSec / 2))
                cnt++;
            return roundToMin * 60 * cnt;
        }
        else
            return unixSec;
    }

    public static long withTime(long unixTime, int hourOfDay, int minute) {
        DateTime dt = new DateTime(unixTime * 1000);
        return dt.withHourOfDay(hourOfDay).withMinuteOfHour(minute).getMillis() / 1000;
    }

    public static long withDate(long unixTime, int year, int month, int dayOfMonth) {
        DateTime dt = new DateTime(unixTime * 1000);
        return dt.withYear(year).withMonthOfYear(month).withDayOfMonth(dayOfMonth).getMillis() / 1000;
    }
}
