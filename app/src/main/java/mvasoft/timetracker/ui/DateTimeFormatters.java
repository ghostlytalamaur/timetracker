package mvasoft.timetracker.ui;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DateTimeFormatters {

    private PeriodFormatter mPeriodFormatter;
    private DateTimeFormatter mDateFormatter;
    private DateTimeFormatter mTimeFormatter;

    private PeriodFormatter getPeriodFormatter() {
        if (mPeriodFormatter == null) {
            mPeriodFormatter = new PeriodFormatterBuilder()
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendHours()
                    .appendSeparator(":")
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendMinutes()
                    .appendSeparator(":")
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendSeconds()
                    .toFormatter();
        }
        return mPeriodFormatter;
    }

    private DateTimeFormatter getDateFormatter() {
        if (mDateFormatter == null) {
            mDateFormatter = new DateTimeFormatterBuilder().
                    appendDayOfWeekText().
                    appendLiteral(", ").
                    appendDayOfMonth(2).
                    appendLiteral(" ").
                    appendMonthOfYearText().
                    appendLiteral(" ").
                    appendYear(4, 4).
                    toFormatter();
        }
        return mDateFormatter;
    }

    private DateTimeFormatter getTimeFormatter() {
        if (mTimeFormatter == null) {
            mTimeFormatter = new DateTimeFormatterBuilder().
                    appendHourOfDay(2).
                    appendLiteral(":").
                    appendMinuteOfHour(2).
                    toFormatter();
        }
        return mTimeFormatter;
    }

    public String formatTime(long unixTimeSec) {
        return getTimeFormatter().print(new DateTime(unixTimeSec * 1000L));
    }

    public String formatDate(long unixTimeSec) {
        return getDateFormatter().print(new DateTime(unixTimeSec * 1000L));
    }

    public String formatPeriod(long secPeriod) {
        return getPeriodFormatter().print(new Period(secPeriod * 1000L));
    }


}
