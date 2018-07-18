package mvasoft.timetracker.ui.widget;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import mvasoft.timetracker.utils.DateTimeHelper;
import timber.log.Timber;

public class SessionsWidgetJobService extends JobService {

    private final static int JOB_ID = 10001;

    @Override
    public boolean onStartJob(JobParameters params) {
        SessionsWidgetService.enqueueUpdate(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public static void scheduleUpdate(@NonNull Context context, boolean hasOpened) {
        JobInfo.Builder b = new JobInfo.Builder(JOB_ID,
                new ComponentName(context, SessionsWidgetJobService.class));

        if (hasOpened) {
            b.setMinimumLatency(TimeUnit.MINUTES.toMillis(1));
            b.setOverrideDeadline(TimeUnit.HOURS.toMillis(3));
        }
        else {
            long today = DateTimeHelper.startOfDay(System.currentTimeMillis() / 1000);
            long deadline = DateTimeHelper.plusDay(today, 1) * 1000;
            long minLatency = Math.min(TimeUnit.HOURS.toMillis(6), deadline);

            b.setMinimumLatency(minLatency);
            b.setOverrideDeadline(Math.max(minLatency, deadline));
        }

        b.setRequiresDeviceIdle(false);
        b.setRequiresCharging(false);

        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) {
            Timber.d("jobScheduler is null");
            return;
        }

        jobScheduler.schedule(b.build());
    }

}
