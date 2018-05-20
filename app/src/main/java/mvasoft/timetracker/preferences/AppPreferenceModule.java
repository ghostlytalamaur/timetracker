package mvasoft.timetracker.preferences;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import mvasoft.timetracker.R;

@Module
public class AppPreferenceModule {

    @Provides
    AppPreferences appPreferencesProvider(Context context) {
        try {
            PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        } catch (Throwable e){
            PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        }
        return new AppPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(context));
    }

}
