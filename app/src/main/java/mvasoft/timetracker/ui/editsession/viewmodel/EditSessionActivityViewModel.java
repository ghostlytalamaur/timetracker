package mvasoft.timetracker.ui.editsession.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;

public class EditSessionActivityViewModel extends BaseViewModel {

    private final DataRepository mRepository;
    private LiveData<List<Long>> mSessionsIds;

    @Inject
    public EditSessionActivityViewModel(@NonNull Application application, DataRepository repository) {
        super(application);
        mRepository = repository;
    }


    public LiveData<List<Long>> getSessionsIds() {
        if (mSessionsIds == null)
            mSessionsIds = mRepository.getSessionsIds();
        return mSessionsIds;
    }
}
