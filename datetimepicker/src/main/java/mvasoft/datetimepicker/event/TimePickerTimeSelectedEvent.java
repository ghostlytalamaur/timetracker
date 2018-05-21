package mvasoft.datetimepicker.event;

public class TimePickerTimeSelectedEvent {
    public final String tag;
    public final int hourOfDay;
    public final int minute;


    public TimePickerTimeSelectedEvent(String tag, int hourOfDay, int minute) {
        this.tag = tag;
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }
}
