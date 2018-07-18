package mvasoft.timetracker.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import timber.log.Timber;

public class SessionsWidget extends AppWidgetProvider {

    @Inject
    WidgetHelper mHelper;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        AndroidInjection.inject(this, context);

        Timber.d("onUpdate");
        mHelper.updateWidget();
    }


}

