package mvasoft.recyclerbinding.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.Objects;

public abstract class ItemViewModel {

    private final MutableLiveData<Boolean> mIsSelectedData;

    protected ItemViewModel() {
        mIsSelectedData = new MutableLiveData<>();
        mIsSelectedData.setValue(false);
    }

    public abstract long getId();

    public LiveData<Boolean> getIsSelected() {
        return mIsSelectedData;
    }

    public boolean isSelected() {
        return Objects.requireNonNull(mIsSelectedData.getValue());
    }

    public void setIsSelected(boolean isSelected) {
        mIsSelectedData.setValue(isSelected);
    }

    public void toggleSelection() {
        mIsSelectedData.setValue(mIsSelectedData.getValue() != null && !mIsSelectedData.getValue());
    }


}
