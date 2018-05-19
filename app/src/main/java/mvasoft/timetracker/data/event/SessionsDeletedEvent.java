package mvasoft.timetracker.data.event;

public class SessionsDeletedEvent {
    public final int deletedSessionsCount;

    public SessionsDeletedEvent(int deletedSessionsCount) {
        this.deletedSessionsCount = deletedSessionsCount;
    }
}
