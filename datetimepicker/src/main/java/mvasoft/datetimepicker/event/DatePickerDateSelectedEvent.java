package mvasoft.datetimepicker.event;

public class DatePickerDateSelectedEvent {
    public final String tag;
    public final int year;

    // starts from 1
    public final int month;
    public final int dayOfMonth;


    public DatePickerDateSelectedEvent(String eventTag, int year, int month, int dayOfMonth) {
        this.tag = eventTag;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }
}