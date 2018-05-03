package mvasoft.timetracker.extlist.modelview;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.SessionHelper;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.base.BaseViewModel;

public class TabbedActivityViewModel extends BaseViewModel {


    private Lazy<DataRepository> mRepository;
    private LiveData<Long> mOpenedSessionId;

    @Inject
    TabbedActivityViewModel(@NonNull Application application, Lazy<DataRepository> repository) {
        super(application);
        mRepository = repository;
    }

    public LiveData<SessionHelper.ToggleSessionResult> toggleSession() {
        return mRepository.get().toggleSession();
    }

    public LiveData<Long> getOpenedSessionsId() {
        if (mOpenedSessionId == null)
            mOpenedSessionId = mRepository.get().getOpenedSessionId();

        return mOpenedSessionId;
    }
}
