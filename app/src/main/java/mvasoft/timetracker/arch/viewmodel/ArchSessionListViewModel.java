package mvasoft.timetracker.arch.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class ArchSessionListViewModel extends AndroidViewModel {

    private final ArchSessionListLiveData mCurrentGroupsData;

    public ArchSessionListViewModel(@NonNull Application application) {
        super(application);
        mCurrentGroupsData = new ArchSessionListLiveData(application);
    }


}
