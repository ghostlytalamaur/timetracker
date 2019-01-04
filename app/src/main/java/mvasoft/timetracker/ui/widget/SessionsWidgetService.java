package mvasoft.timetracker.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.widget.RemoteViews;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.android.AndroidInjection;
import mvasoft.timetracker.R;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.NavigationDrawerActivity;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.DayGroupsUtils;
import timber.log.Timber;

import static mvasoft.timetracker.utils.DateTimeFormatters.DateTimeFormattersType.dtft_Clipboard;

public class SessionsWidgetService extends JobIntentService {

    private static final int JOB_ID = 1000;

    @Inject
    Lazy<DataRepository> mRepository;

    private DateTimeFormatters mFormatters;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate()");
        AndroidInjection.inject(this);
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Timber.d("onHandleWork()");
        String action = intent.getAction();
        if (action == null)
            return;

        Timber.d("onHandleIntent: %s", action);
        switch (action) {
            case WidgetActions.ACTION_WIDGET_UPDATE: {
                updateWidget();
                break;
            }
            case WidgetActions.ACTION_WIDGET_TOGGLE_SESSION: {
                mRepository.get().toggleSession();
                break;
            }
            default:
                throw new UnsupportedOperationException(
                        String.format("Action %s not supported.", action));
        }

    }

    public static void enqueueUpdate(Context context) {
        Intent intent = new Intent(context, SessionsWidgetService.class);
        intent.setAction(WidgetActions.ACTION_WIDGET_UPDATE);

        enqueueWork(context, SessionsWidgetService.class, JOB_ID, intent);
        Timber.d("enqueueUpdate()");
    }

    public static void enqueueToggleSession(Context context) {
        Intent intent = new Intent(context, SessionsWidgetService.class);
        intent.setAction(WidgetActions.ACTION_WIDGET_TOGGLE_SESSION);

        enqueueWork(context, SessionsWidgetService.class, JOB_ID, intent);
        Timber.d("enqueueToggleSession()");
    }

    public DateTimeFormatters getFormatters() {
        if (mFormatters == null)
            mFormatters = new DateTimeFormatters(dtft_Clipboard);
        return mFormatters;
    }

    private void updateWidget() {
        long today = System.currentTimeMillis() / 1000;
        List<Long> days = DateTimeHelper.daysList(
                DateTimeHelper.startOfMonthWeek(today), DateTimeHelper.endOfMonthWeek(today));
        updateWidget(mRepository.get().getDayGroupsRx(days)
                .first(Collections.emptyList())
                .blockingGet());
    }

    private void updateWidget(List<DayGroup> groups) {
        AppWidgetManager widgetMan = AppWidgetManager.getInstance(this);
        int[] ids = widgetMan.getAppWidgetIds(new ComponentName(this, SessionsWidget.class));
        if (ids.length <= 0) {
            return;
        }

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.sessions_widget);

        bindViews(views, groups);
        widgetMan.updateAppWidget(ids, views);
    }

    private void bindViews(RemoteViews views, List<DayGroup> groups) {
        if (views == null)
            return;

        Timber.d("bindViews()");
        DayGroupsUtils.sortGroups(groups);
        // Bind text views
        String todayText;
        String stateText;
        String weekText;
        int btnImageId;

        DayGroup todayGroup = DayGroupsUtils.getDayGroupForDay(groups,
                System.currentTimeMillis() / 1000);
        if (todayGroup != null) {
            todayText = String.format(getString(R.string.appwidget_text_today),
                    getFormatters().formatDuration(todayGroup.getDuration()));

        } else {
            todayText = getString(R.string.appwidget_text_today_empty);
        }

        if (groups.size() > 0) {
            weekText = String.format(getString(R.string.appwidget_text_week),
                    getFormatters().formatDuration(DayGroupsUtils.getDuration(groups)));
        }
        else {
            weekText = getString(R.string.appwidget_text_week_empty);
        }

        boolean hasOpened = mRepository.get().getOpenedSessionsIds()
                .first(Collections.emptyList())
                .blockingGet().size() > 0;
        if (hasOpened) {
            stateText = getString(R.string.appwidget_text_state_opened);
            btnImageId = R.drawable.minus;
        }
        else {
            stateText = getString(R.string.appwidget_text_state_closed);
            btnImageId = R.drawable.plus;
        }

        views.setImageViewResource(R.id.appwidget_button, btnImageId);
        views.setTextViewText(R.id.appwidget_text_state, stateText);
        views.setTextViewText(R.id.appwidget_text_week, weekText);
        views.setTextViewText(R.id.appwidget_text_today, todayText);

        // Setup click listeners
        Intent intent = new Intent(this, JobReceiver.class);
        intent.setAction(WidgetActions.ACTION_WIDGET_TOGGLE_SESSION);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_button, pendingIntent);

        intent = new Intent(this, NavigationDrawerActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text_layout, pendingIntent);

        SessionsWidgetJobService.scheduleUpdate(this, hasOpened);
    }
}
