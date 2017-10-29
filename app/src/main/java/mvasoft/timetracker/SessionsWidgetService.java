package mvasoft.timetracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Calendar;

public class SessionsWidgetService extends ExtService {

    public static final String ACTION_UPDATE_WIDGET = "mvasoft.timetracker.action.update_widget.new";
    private static final String LOGT = "mvasoft.timetracker.log";
    private static final String ACTION_TOGGLE_SESSION = "mvasoft.timetracker.action.toggle_session";

    private SessionHelper mSessionHelper;
    private PeriodFormatter mPeriodFormatter;
    private PendingIntent mAlarmPendingIntent;

    public SessionsWidgetService() {
        super("SessionsWidgetService");
        Log.d(LOGT, "SessionsWidgetService()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOGT, "SessionsWidgetService.onCreate()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOGT, "SessionsWidgetService.onDestroy()");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOGT, "SessionsWidgetService.onHandleIntent()");
        if (intent == null)
            return;

        String action = intent.getAction();
        if (ACTION_UPDATE_WIDGET.equals(action))
            updateWidget();
        else if (ACTION_TOGGLE_SESSION.equals(action))
            getSessionHelper().toggleSession();
    }

    private void updateWidget() {
        Log.d(LOGT, "SessionsWidgetService.updateWidget()");

        AppWidgetManager widgetMan = AppWidgetManager.getInstance(this);

        int[] ids = widgetMan.getAppWidgetIds(new ComponentName(this, SessionsWidget.class));
        if (ids.length <= 0) {
            stopSelf();
            return;
        }

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.sessions_widget);

        bindViews(views);
        widgetMan.updateAppWidget(ids, views);

        updateAlarm();
    }

    private SessionHelper getSessionHelper() {
        if (mSessionHelper == null)
            mSessionHelper = new SessionHelper(this);
        return mSessionHelper;
    }

    private void bindViews(RemoteViews views) {
        if (views == null)
            return;

        // Setup click listeners
        Intent intent = new Intent(this, SessionsWidgetService.class);
        intent.setAction(ACTION_TOGGLE_SESSION);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);

        intent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text_layout, pendingIntent);

        // Bind text views
        String todayText;
        long duration = getSessionHelper().getTodayDuration();
        if (duration != SessionHelper.EMPTY_DURATION)
            todayText = String.format(getString(R.string.appwidget_text_today),
                    getPeriodFormatter().print(new Period(duration * 1000)));
        else
            todayText = getString(R.string.appwidget_text_today_empty);

        String stateText;
        String currentText;
        int btnImageId;
        boolean hasOpened = getSessionHelper().hasOpenedSessions();
        if (hasOpened) {
            duration = getSessionHelper().getCurrentDuration();
            currentText = String.format(getString(R.string.appwidget_text_current),
                    getPeriodFormatter().print(new Period(duration * 1000)));

            stateText = getString(R.string.appwidget_text_state_opened);
            btnImageId = R.drawable.minus;
        } else {
            stateText = getString(R.string.appwidget_text_state_closed);
            currentText = getString(R.string.appwidget_text_current_empty);
            btnImageId = R.drawable.plus;
        }

        views.setImageViewResource(R.id.appwidget_button, btnImageId);
        views.setTextViewText(R.id.appwidget_text_state, stateText);
        views.setTextViewText(R.id.appwidget_text_current, currentText);
        views.setTextViewText(R.id.appwidget_text_today, todayText);
    }

    private void updateAlarm() {
        boolean hasOpened = getSessionHelper().hasOpenedSessions();
        boolean shouldStop = true;
        try {
            if ((mAlarmPendingIntent == null) && hasOpened) {
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager == null)
                    return;

                Intent alarm_intent = new Intent(this, SessionsWidgetService.class);
                alarm_intent.setAction(ACTION_UPDATE_WIDGET);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                mAlarmPendingIntent = PendingIntent.getService(this, 0, alarm_intent, 0);
                alarmManager.setRepeating(AlarmManager.RTC, cal.getTime().getTime(), 60 * 1000, mAlarmPendingIntent);
                shouldStop = false;
            } else if (!hasOpened && (mAlarmPendingIntent != null)) {
                mAlarmPendingIntent.cancel();
                mAlarmPendingIntent = null;
            }
        } finally {
          if (shouldStop)
              stopSelf();
        }
    }

    private PeriodFormatter getPeriodFormatter() {
        if (mPeriodFormatter == null)
            mPeriodFormatter = new PeriodFormatterBuilder().
                    printZeroAlways().
                    minimumPrintedDigits(2).
                    appendHours().
                    appendSeparator(":").
                    printZeroAlways().
                    minimumPrintedDigits(2).
                    appendMinutes().
                    toFormatter();

        return mPeriodFormatter;
    }
}
