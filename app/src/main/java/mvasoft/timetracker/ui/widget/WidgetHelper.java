package mvasoft.timetracker.ui.widget;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WidgetHelper implements IWidgetHelper {

    private final Context mContext;

    @Inject
    WidgetHelper(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void updateWidget() {
        mContext.startService(SessionsWidgetService.makeUpdateIntent(mContext));
    }
}
