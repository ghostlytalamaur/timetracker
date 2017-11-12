package mvasoft.timetracker.widget;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;
import javax.inject.Singleton;

import mvasoft.timetracker.SessionsWidgetService;

@Singleton
public class WidgetHelper implements IWidgetHelper {

    private Context mContext;

    @Inject
    WidgetHelper(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void updateWidget() {
        Intent serviceIntent = new Intent(mContext, SessionsWidgetService.class);
        serviceIntent.setAction(SessionsWidgetService.ACTION_UPDATE_WIDGET);
        mContext.startService(serviceIntent);
    }
}
