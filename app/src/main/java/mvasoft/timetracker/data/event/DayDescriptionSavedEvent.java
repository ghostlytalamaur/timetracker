package mvasoft.timetracker.data.event;

public class DayDescriptionSavedEvent {
    public final boolean wasSaved;

    public DayDescriptionSavedEvent(boolean wasSaved) {
        this.wasSaved = wasSaved;
    }
}
