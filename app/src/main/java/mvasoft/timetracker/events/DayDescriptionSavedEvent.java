package mvasoft.timetracker.events;

public class DayDescriptionSavedEvent {
    public final boolean wasSaved;

    public DayDescriptionSavedEvent(boolean wasSaved) {
        this.wasSaved = wasSaved;
    }
}
