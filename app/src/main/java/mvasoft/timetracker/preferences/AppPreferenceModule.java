package mvasoft.timetracker.preferences;

import android.content.Context;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import mvasoft.timetracker.R;

@Module
public class AppPreferenceModule {

    @Provides
    AppPreferences appPreferencesProvider(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        return new AppPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(context));
    }

}
