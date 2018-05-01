package mvasoft.timetracker.ui.base;

import android.app.Application;
import android.support.annotation.NonNull;

import mvasoft.timetracker.databinding.BindableAndroidViewModel;

public class BaseViewModel extends BindableAndroidViewModel {

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }
}
