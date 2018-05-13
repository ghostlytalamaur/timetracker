package mvasoft.timetracker.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class SessionsWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, SessionsWidgetService.class);
        intent.setAction(SessionsWidgetService.ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }
}

