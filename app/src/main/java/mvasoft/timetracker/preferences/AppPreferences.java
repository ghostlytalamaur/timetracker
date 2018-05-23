package mvasoft.timetracker.preferences;

public abstract class AppPreferences {


    /**
     *
     * @return default target time in minutes
     */
    public abstract long getTargetTimeInMin();
    public abstract boolean isWorkingDay(int dayNumber);
    public abstract long roundDurationToMin();
    public abstract boolean syncStartEndDate();
}
