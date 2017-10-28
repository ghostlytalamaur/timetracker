package mvasoft.timetracker;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;


public class SessionsService extends IntentService {

    public static final String ACTION_TOGGLE_SESSION = "mvasoft.timetracker.action.toggle_session";
    public static final String ACTION_UPDATE_WIDGET = "mvasoft.timetracker.action.update_widget";


    public SessionsService() {
        super("SessionsService");
    }

    public static void startActionToggleSession(Context context) {
        Intent intent = new Intent(context, SessionsService.class);
        intent.setAction(ACTION_TOGGLE_SESSION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TOGGLE_SESSION.equals(action)) {
                toggleSession();
            }
            else if (ACTION_UPDATE_WIDGET.equals(action))
                updateWidget();
        }
    }

    private void toggleSession() {
        SessionHelper helper = new SessionHelper(this);
        helper.toggleSession();
        updateWidget();
    }

    private void updateWidget() {
//        Intent intent = new Intent(this, SessionsWidget.class);
//        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
//                AppWidgetManager.getInstance(this).getAppWidgetIds(
//                        new ComponentName(this, SessionsWidget.class)));
//        sendBroadcast(intent);
        SessionsWidget.updateAppWidgets(this);
    }

}
