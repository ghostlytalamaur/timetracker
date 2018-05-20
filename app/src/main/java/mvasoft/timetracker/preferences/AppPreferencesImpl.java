package mvasoft.timetracker.preferences;

import android.content.SharedPreferences;

import java.util.Set;

public class AppPreferencesImpl extends AppPreferences {

    private static final String PREF_VERSION_KEY = "pref_version";
    private static final int PREF_VERSION_VALUE = 1;

    private final SharedPreferences mPreferences;

    AppPreferencesImpl(SharedPreferences preferences) {
        mPreferences = preferences;
        upgradeVersions();
    }

    private void upgradeVersions() {
        SharedPreferences.Editor editor = mPreferences.edit();
        switch (mPreferences.getInt(PREF_VERSION_KEY, 0)) {
            case 0:
                editor.clear().apply();
                break;
        }
        editor.putInt(PREF_VERSION_KEY, PREF_VERSION_VALUE).apply();
    }


    @Override
    public long getTargetTimeInMin() {
        return (long) mPreferences.getInt("pref_target_time_min", 0);
    }

    @Override
    public boolean isWorkingDay(int dayNumber) {
        Set<String> workingDays = mPreferences.getStringSet("pref_working_days", null);
        return (workingDays != null) && workingDays.contains(String.valueOf(dayNumber));
    }

    @Override
    public long roundDurationToMin() {
        return mPreferences.getInt("pref_round_duration_to_min", 0);
    }
}
