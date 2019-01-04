package mvasoft.timetracker.ui.common;

import android.app.Application;
import androidx.lifecycle.LifecycleObserver;
import androidx.annotation.NonNull;

import mvasoft.timetracker.databinding.BindableAndroidViewModel;

public class BaseViewModel extends BindableAndroidViewModel implements LifecycleObserver {

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }
}
