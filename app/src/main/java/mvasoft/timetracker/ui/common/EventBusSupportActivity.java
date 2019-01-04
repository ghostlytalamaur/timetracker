package mvasoft.timetracker.ui.common;

import org.greenrobot.eventbus.EventBus;

import androidx.appcompat.app.AppCompatActivity;
import dagger.android.support.DaggerAppCompatActivity;

public abstract class EventBusSupportActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldRegisterToEventBus())
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (shouldRegisterToEventBus())
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    protected boolean shouldRegisterToEventBus() {
        return false;
    }
}
