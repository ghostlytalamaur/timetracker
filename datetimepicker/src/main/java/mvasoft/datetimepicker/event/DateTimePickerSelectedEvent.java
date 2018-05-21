package mvasoft.datetimepicker.event;

public class DateTimePickerSelectedEvent {

    public final long unixTime;

    public DateTimePickerSelectedEvent(long unixTime) {
        this.unixTime = unixTime;
    }
}
