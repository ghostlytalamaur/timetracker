package mvasoft.timetracker.arch.viewmodel;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import mvasoft.timetracker.GroupsList;

public class ArchSessionListLiveData extends LiveData<GroupsList> {

    private final Context mContext;

    public ArchSessionListLiveData(Context aContext) {
        super();
        mContext = aContext;
    }
}
