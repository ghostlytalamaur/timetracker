package mvasoft.timetracker.data.event;

public class SessionSavedEvent {
    public final boolean wasSaved;

    public SessionSavedEvent(boolean wasSaved) {
        this.wasSaved = wasSaved;
    }
}
