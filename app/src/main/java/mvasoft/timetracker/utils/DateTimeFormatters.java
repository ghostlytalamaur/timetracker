package mvasoft.timetracker.utils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DateTimeFormatters {

    private final DateTimeFormattersType mType;
    private PeriodFormatter mPeriodFormatter;
    private DateTimeFormatter mDateFormatter;
    private DateTimeFormatter mTimeFormatter;

    public DateTimeFormatters() {
        this(DateTimeFormattersType.dtft_Default);
    }

    public DateTimeFormatters(DateTimeFormattersType type) {
        mType = type;
    }

    private PeriodFormatter getPeriodFormatter() {
        if (mPeriodFormatter == null) {
            PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendHours()
                    .appendSeparator(":")
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendMinutes()
                    .appendSeparator(":")
                    .printZeroAlways()
                    .minimumPrintedDigits(2);
            if (mType == DateTimeFormattersType.dtft_Default)
                builder.appendSeconds();
            mPeriodFormatter = builder.toFormatter();
        }
        return mPeriodFormatter;
    }

    private DateTimeFormatter getDateFormatter() {
        if (mDateFormatter == null) {
            mDateFormatter = new DateTimeFormatterBuilder().
                    appendDayOfWeekShortText().
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

    public String formatDuration(long secDuration) {
        return (secDuration < 0 ? "-" : "") +
                getPeriodFormatter().print(new Period(Math.abs(secDuration) * 1000L));
    }


    public enum DateTimeFormattersType {
        dtft_Default,
        dtft_Clipboard
    }
}
