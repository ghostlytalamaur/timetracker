package mvasoft.timetracker.preferences;

public abstract class AppPreferences {


    public abstract long getTargetTime();
    public abstract boolean isWorkingDay(int dayNumber);
}
