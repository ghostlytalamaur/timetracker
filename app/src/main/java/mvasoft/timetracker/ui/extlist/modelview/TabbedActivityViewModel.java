package mvasoft.timetracker.ui.extlist.modelview;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;

public class TabbedActivityViewModel extends BaseViewModel {


    private Lazy<DataRepository> mRepository;
    private LiveData<Long> mOpenedSessionId;
    private MutableLiveData<Long> mCurrentDateLiveData;

    @Inject
    TabbedActivityViewModel(@NonNull Application application, Lazy<DataRepository> repository) {
        super(application);
        mRepository = repository;
    }

    public LiveData<DataRepository.ToggleSessionResult> toggleSession() {
        return mRepository.get().toggleSession();
    }

    public LiveData<Long> getOpenedSessionsId() {
        if (mOpenedSessionId == null)
            mOpenedSessionId = mRepository.get().getOpenedSessionId();

        return mOpenedSessionId;
    }

    public LiveData<Long> getCurrentDateLiveData() {
        if (mCurrentDateLiveData == null)
            mCurrentDateLiveData = new MutableLiveData<Long>();
        return mCurrentDateLiveData;
    }
}
