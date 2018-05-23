package mvasoft.timetracker.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import java.util.Set;

import mvasoft.timetracker.R;

public class AppPreferencesImpl extends AppPreferences {

    private static final String PREF_VERSION_KEY = "pref_version";
    private static final int PREF_VERSION_VALUE = 1;

    private final SharedPreferences mPreferences;
    private final Keys mKeys;

    AppPreferencesImpl(@NonNull Context context) {
        mKeys = new Keys(context);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        setDefaults(context);
        upgradeVersions();
    }

    private void setDefaults(@NonNull Context context) {
        try {
            PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        } catch (Throwable e){
            mPreferences.edit().clear().apply();
            PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        }
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
        return (long) mPreferences.getInt(mKeys.targetMin, 0);
    }

    @Override
    public boolean isWorkingDay(int dayNumber) {
        Set<String> workingDays = mPreferences.getStringSet(mKeys.workingDay, null);
        return (workingDays != null) && workingDays.contains(String.valueOf(dayNumber));
    }

    @Override
    public long roundDurationToMin() {
        return mPreferences.getInt(mKeys.roundDuration, 0);
    }

    @Override
    public boolean syncStartEndDate() {
        return mPreferences.getBoolean(mKeys.syncStartEndDate, true);
    }

    private static class Keys {
        final String workingDay;
        final String targetMin;
        final String roundDuration;
        final String syncStartEndDate;

        Keys(@NonNull Context context) {
            workingDay = context.getString(R.string.prefs_key_working_days);
            targetMin = context.getString(R.string.prefs_key_target_time_min);
            roundDuration = context.getString(R.string.prefs_key_round_duration_to_min);
            syncStartEndDate = context.getString(R.string.prefs_key_sync_start_end_date);
        }
    }
}
