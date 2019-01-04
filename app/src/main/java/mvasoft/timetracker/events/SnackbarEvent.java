package mvasoft.timetracker.events;

import androidx.annotation.StringRes;

public class SnackbarEvent {
    public final @StringRes
    int message;
    public final int duration;

    public SnackbarEvent(@StringRes int message, int duration) {
        this.message = message;
        this.duration = duration;
    }
}
