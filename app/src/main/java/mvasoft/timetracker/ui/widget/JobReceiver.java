package mvasoft.timetracker.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class JobReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        Timber.d("received action %s", action);
        switch (action) {
            case WidgetActions.ACTION_WIDGET_UPDATE: {
                SessionsWidgetService.enqueueUpdate(context);
                break;
            }
            case WidgetActions.ACTION_WIDGET_TOGGLE_SESSION: {
                SessionsWidgetService.enqueueToggleSession(context);
                break;
            }

            default:
                throw new UnsupportedOperationException(
                        String.format("Action %s not supported.", action));
        }
    }
}
