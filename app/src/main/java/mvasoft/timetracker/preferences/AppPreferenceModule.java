package mvasoft.timetracker.preferences;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class AppPreferenceModule {

    @Provides
    AppPreferences appPreferencesProvider(Context context) {
        return new AppPreferencesImpl(context);
    }

}
