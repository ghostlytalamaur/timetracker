package mvasoft.timetracker.extlist.modelview;

import android.app.Application;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.android.databinding.library.baseAdapters.BR;

import mvasoft.timetracker.SessionHelper;
import mvasoft.timetracker.ui.base.BaseViewModel;

public class TabbedActivityViewModel extends BaseViewModel {

    private SessionHelper mHelper;

    public TabbedActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public void toggleSession() {
        getHelper().toggleSession();
        notifyPropertyChanged(BR.hasOpenedSessions);
    }

    @Bindable
    public boolean getHasOpenedSessions() {
        return getHelper().hasOpenedSessions();
    }

    private SessionHelper getHelper() {
        if (mHelper == null)
            mHelper = new SessionHelper(getApplication());
        return mHelper;
    }
}
