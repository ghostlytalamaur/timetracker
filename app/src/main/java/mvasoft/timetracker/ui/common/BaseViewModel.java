package mvasoft.timetracker.ui.common;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.annotation.NonNull;

public class BaseViewModel extends AndroidViewModel implements DefaultLifecycleObserver {

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }
}
