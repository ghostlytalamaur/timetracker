package mvasoft.timetracker.ui.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import dagger.Lazy;
import dagger.android.DaggerIntentService;
import dagger.android.DaggerService;
import io.reactivex.Flowable;
import mvasoft.timetracker.R;
import mvasoft.timetracker.core.ExtService;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.extlist.TabbedActivity;
import mvasoft.timetracker.vo.DayGroup;

public class SessionsWidgetService extends ExtService {

    private static final String ACTION_UPDATE_WIDGET = "mvasoft.timetracker.action.update_widget.new";
    private static final String ACTION_TOGGLE_SESSION = "mvasoft.timetracker.action.toggle_session";

    private PeriodFormatter mPeriodFormatter;
    private PendingIntent mAlarmPendingIntent;
    public Lazy<DataRepository> mRepository;

    public SessionsWidgetService() {
        super("SessionsWidgetService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Intent makeUpdateIntent(Context context) {
        Intent intent = new Intent(context, SessionsWidgetService.class);
        intent.setAction(SessionsWidgetService.ACTION_UPDATE_WIDGET);
        return intent;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();
        if (ACTION_UPDATE_WIDGET.equals(action))
            updateWidget();
        else if (ACTION_TOGGLE_SESSION.equals(action))
            mRepository.get().toggleSession();
    }

    private void updateWidget() {
        AppWidgetManager widgetMan = AppWidgetManager.getInstance(this);

        LiveData<List<DayGroup>> groups =
                mRepository.get().getDayGroups(Arrays.asList(System.currentTimeMillis() / 1000));

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

    private void bindViews(RemoteViews views) {
        if (views == null)
            return;

        // Setup click listeners
        Intent intent = new Intent(this, SessionsWidgetService.class);
        intent.setAction(ACTION_TOGGLE_SESSION);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);

        intent = new Intent(this, TabbedActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text_layout, pendingIntent);

        // Bind text views
        String todayText;
        long duration = 0;//getSessionHelper().getTodayDuration();
        if (duration != 0)
            todayText = String.format(getString(R.string.appwidget_text_today),
                    getPeriodFormatter().print(new Period(duration * 1000)));
        else
            todayText = getString(R.string.appwidget_text_today_empty);

        String stateText;
        String currentText;
        int btnImageId;
        boolean hasOpened = false; //getSessionHelper().hasOpenedSessions();
        if (hasOpened) {
            duration = 0; //getSessionHelper().getCurrentDuration();
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
        boolean hasOpened = false; //getSessionHelper().hasOpenedSessions();
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
