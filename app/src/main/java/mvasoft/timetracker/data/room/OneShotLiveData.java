package mvasoft.timetracker.data.room;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

class OneShotLiveData<T> extends LiveData<T> {

    OneShotLiveData(LiveData<T> source) {
        source.observeForever(new Observer<T>() {
            @Override
            public void onChanged(@Nullable T value) {
                postValue(value);
                removeObserver(this);
            }
        });
    }

}
