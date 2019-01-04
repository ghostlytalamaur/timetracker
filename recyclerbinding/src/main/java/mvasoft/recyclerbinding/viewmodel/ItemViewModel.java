package mvasoft.recyclerbinding.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.EventListener;
import java.util.Objects;

import mvasoft.utils.Announcer;

public abstract class ItemViewModel {

    private Announcer<ItemViewModelSelectionListener> mAnnouncer;

    private final MutableLiveData<Boolean> mIsSelectedData;

    protected ItemViewModel() {
        mIsSelectedData = new MutableLiveData<>();
        mIsSelectedData.postValue(false);
        mAnnouncer = new Announcer<>(ItemViewModelSelectionListener.class);
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
        mAnnouncer.announce().selectionChanged(this);

    }

    public void toggleSelection() {
        setIsSelected(!isSelected());
    }

    void addSelectionListener(ItemViewModelSelectionListener listener) {
        mAnnouncer.addListener(listener);
    }

    void removeSelectionListener(ItemViewModelSelectionListener listener) {
        mAnnouncer.removeListener(listener);
    }

    interface ItemViewModelSelectionListener extends EventListener {
        void selectionChanged(ItemViewModel item);
    }
}
