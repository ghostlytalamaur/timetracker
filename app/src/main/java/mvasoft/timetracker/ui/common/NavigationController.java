package mvasoft.timetracker.ui.common;


public interface NavigationController {
    void editSession(long sessionId);
    void editDate(long unixTime);
    void navigateToSessions();
    void navigateToDates();
    void navigateToSettings();
    void navigateToBackup();
}
