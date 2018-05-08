package mvasoft.timetracker.preferences;

import android.content.SharedPreferences;

import java.util.Set;

public class AppPreferencesImpl extends AppPreferences {

    private SharedPreferences mPreferences;

    AppPreferencesImpl(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    @Override
    public long getTargetTime() {
        return Long.valueOf(mPreferences.getString("pref_target_time_min", "0")) * 60;
    }

    @Override
    public boolean isWorkingDay(int dayNumber) {
        Set<String> workingDays = mPreferences.getStringSet("pref_working_days", null);
        return (workingDays != null) && workingDays.contains(String.valueOf(dayNumber));
    }
}
