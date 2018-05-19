package mvasoft.datetimepicker.event;

public class DatePickerDateSelectedEvent {
    public final long unixTime;


    protected DatePickerDateSelectedEvent(long unixTime) {
        this.unixTime = unixTime;
    }
}