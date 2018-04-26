package mvasoft.timetracker.ui;

import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import mvasoft.timetracker.SessionEditData;
import mvasoft.timetracker.ui.base.BaseViewModel;


public class SessionEditViewModel extends BaseViewModel {

    private PeriodFormatter mPeriodFormatter;
    private DateTimeFormatter mDateTimeFormatter;
    private Cursor mCursor;
    private SessionEditData mData;
    public SessionEditViewModel(SessionEditData data) {
        super();

        mData = data;
        mPeriodFormatter = new PeriodFormatterBuilder().
                printZeroAlways().
                minimumPrintedDigits(2).
                appendHours().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendMinutes().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendSeconds().
                toFormatter();

        mDateTimeFormatter = new DateTimeFormatterBuilder().
                appendDayOfWeekText().
                appendLiteral(", ").
                appendDayOfMonth(2).
                appendLiteral(" ").
                appendMonthOfYearText().
                appendLiteral(" ").
                appendYear(4, 4).
                appendLiteral(" ").
                appendHourOfDay(2).
                appendLiteral(":").
                appendMinuteOfHour(2).
                toFormatter();
    }

    public String getStartText() {
        return mDateTimeFormatter.print(new DateTime(mData.getStartTime() * 1000L));
    }

    public String getEndText() {
        if (mData.isClosed())
            return mDateTimeFormatter.print(new DateTime(mData.getEndTime() * 1000L));
        else
            return "Unclosed";
    }

    public String getDurationText() {
        if (mData.isClosed())
            return mPeriodFormatter.print(
                        new Period((mData.getDuration()) * 1000L ));
        else
            return "";
    }

    public boolean shouldSave() {
        return mData.isChanged();
    }
}
