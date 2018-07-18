package mvasoft.timetracker.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class WidgetHelperImpl implements WidgetHelper {

    private final Context mContext;

    @Inject
    WidgetHelperImpl(@NonNull Context context) {
        super();
        mContext = context.getApplicationContext();
    }

    @Override
    public void updateWidget() {
        Timber.d("updateWidget()");

        Intent intent = new Intent(mContext, JobReceiver.class);
        intent.setAction(WidgetActions.ACTION_WIDGET_UPDATE);

        mContext.sendBroadcast(intent);
    }
}
