package mvasoft.timetracker.ui;

import android.databinding.Bindable;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.SessionEditData;
import mvasoft.timetracker.ui.base.BaseViewModel;


public class SessionEditViewModel extends BaseViewModel {

    private PeriodFormatter mPeriodFormatter;
    private DateTimeFormatter mDateTimeFormatter;
    private SessionEditData mData;
    public SessionEditViewModel(SessionEditData data) {
        super();

        mData = data;
        mData.addDataChangedListener(new SessionDataChangedListener());
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

    @Bindable
    public String getStartTime() {
        return mDateTimeFormatter.print(new DateTime(mData.getStartTime() * 1000L));
    }

    @Bindable
    public String getEndTime() {
        return mDateTimeFormatter.print(new DateTime(mData.getEndTime() * 1000L));
    }

    @Bindable
    public String getDuration() {
        return mPeriodFormatter.print(
                    new Period((mData.getDuration()) * 1000L ));
    }

    @Bindable
    public boolean getIsChanged() {
        return mData.isChanged();
    }

    @Bindable
    public boolean getIsClosed() {
        return mData.isClosed();
    }

    public void setIsClosed(boolean isClosed) {
        mData.setIsClosed(isClosed);
    }

    private class SessionDataChangedListener implements SessionEditData.ISessionDataChangedListener {

        @Override
        public void dataChanged(SessionEditData.SessionDataType dataType) {
            switch (dataType) {
                case sdtAll:
                    notifyPropertyChanged(BR._all);
                    break;
                case sdtStartTime:
                    notifyPropertyChanged(BR.startTime);
                    break;
                case sdtEndTime:
                    notifyPropertyChanged(BR.endTime);
                    break;
                case sdtClosed:
                    notifyPropertyChanged(BR.isClosed);
                    break;
            }

            notifyPropertyChanged(BR.isChanged);
        }
    }
}
