package mvasoft.timetracker.vo;

public interface TimeInfoProvider {

    long getId();
    long getStartTime();
    long getEndTime();
    long getDuration();

    boolean isRunning();
}
