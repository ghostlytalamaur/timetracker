package mvasoft.timetracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Calendar;


public class SessionsWidget extends AppWidgetProvider {

    private PendingIntent mAlarmPendingIntent;

    static void updateAppWidgets(Context context) {
        Log.d("mvasoft.timetracker", "SessionsWidget.updateAppWidgets()");
        if (context == null)
            return;

        AppWidgetManager widgetMan = AppWidgetManager.getInstance(context);

        int[] ids = widgetMan.getAppWidgetIds(new ComponentName(context, SessionsWidget.class));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sessions_widget);

        bindViews(context, views);

        widgetMan.updateAppWidget(ids, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("mvasoft.timetracker", "SessionsWidget.onUpdate()");

        // There may be multiple widgets active, so update all of them
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sessions_widget);
        doUpdateWidget(context, views);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private void doUpdateWidget(Context context, RemoteViews views) {

        if ((context == null) || (views == null))
            return;

        bindViews(context, views);

        // Setup click listeners
        Intent intent = new Intent(context, SessionsService.class);
        intent.setAction(SessionsService.ACTION_TOGGLE_SESSION);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);

        intent = new Intent(context, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text_layout, pendingIntent);

        // TODO: disable alarm when no opened sessions
        // Setup alarm manager for update
        if (mAlarmPendingIntent == null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alaram_intent = new Intent(context, SessionsService.class);
            alaram_intent.setAction(SessionsService.ACTION_UPDATE_WIDGET);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            mAlarmPendingIntent = PendingIntent.getService(context, 0, alaram_intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC, cal.getTime().getTime(), 60 * 1000, mAlarmPendingIntent);
        }
    }

    private static void bindViews(Context context, RemoteViews views) {

        SessionHelper sessionHelper = new SessionHelper(context);
        PeriodFormatter periodFormatter = createPeriodFormatter();

        String todayText;
        long duration = sessionHelper.getTodayDuration();
        if (duration != SessionHelper.EMPTY_DURATION)
            todayText = String.format(context.getString(R.string.appwidget_text_today),
                    periodFormatter.print(new Period(duration * 1000)));
        else
            todayText = context.getString(R.string.appwidget_text_today_empty);

        String stateText;
        String currentText;
        int btnImageId;
        boolean hasOpened = sessionHelper.hasOpenedSessions();
        if (hasOpened) {
            duration = sessionHelper.getCurrentDuration();
            currentText = String.format(context.getString(R.string.appwidget_text_current),
                    periodFormatter.print(new Period(duration * 1000)));

            stateText = context.getString(R.string.appwidget_text_state_opened);
            btnImageId = R.drawable.minus;
        }
        else {
            stateText = context.getString(R.string.appwidget_text_state_closed);
            currentText = context.getString(R.string.appwidget_text_current_empty);
            btnImageId = R.drawable.plus;
        }

        views.setImageViewResource(R.id.appwidget_button, btnImageId);
        views.setTextViewText(R.id.appwidget_text_state, stateText);
        views.setTextViewText(R.id.appwidget_text_current, currentText);
        views.setTextViewText(R.id.appwidget_text_today, todayText);
    }

    private static PeriodFormatter createPeriodFormatter() {
        return new PeriodFormatterBuilder().
                printZeroAlways().
                minimumPrintedDigits(2).
                appendHours().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendMinutes().
                toFormatter();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.d("mvasoft.timetracker", "SessionsWidget.onDeleted()");
        if (mAlarmPendingIntent != null)
            mAlarmPendingIntent.cancel();
    }

}

