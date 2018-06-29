package mvasoft.recyclerbinding.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ListViewModel {

    private final MutableLiveData<List<ItemViewModel>> mItemsLiveData;
    private final MutableLiveData<Boolean> mHasSelectionData;
    private final ItemViewModel.ItemViewModelSelectionListener mSelectionListener;
    private final HashSet<Long> mSelectedIds;
    private final Handler mHandler;

    public ListViewModel() {
        mHandler = new Handler(Looper.getMainLooper());
        mItemsLiveData = new MutableLiveData<>();
        mSelectedIds = new HashSet<>();
        mHasSelectionData = new MutableLiveData<>();
        mHasSelectionData.setValue(false);

        mSelectionListener = item -> {
            if (item.isSelected())
                mSelectedIds.add(item.getId());
            else
                mSelectedIds.remove(item.getId());
            mHasSelectionData.setValue(mSelectedIds.size() > 0);
        };
    }

    private void addSelectionObserver() {
        List<ItemViewModel> list = mItemsLiveData.getValue();
        if (list == null)
            return;

        for (ItemViewModel item : list) {
            item.addSelectionListener(mSelectionListener);
        }
    }

    private void removeSelectionObserver() {
        List<ItemViewModel> list = mItemsLiveData.getValue();
        if (list == null)
            return;

        for (ItemViewModel item : list) {
            item.removeSelectionListener(mSelectionListener);
        }
    }

    public void setItemsList(List<ItemViewModel> list) {
        mHandler.post(() -> {
            removeSelectionObserver();
            try {
                if (list != null) {

                    HashSet<Long> newIds = new HashSet<>();
                    for (ItemViewModel item : list) {
                        newIds.add(item.getId());
                        item.setIsSelected(mSelectedIds.contains(item.getId()));
                    }

                    Iterator<Long> iter = mSelectedIds.iterator();
                    while (iter.hasNext()) {
                        if (!newIds.contains(iter.next())) {
                            iter.remove();
                        }
                    }
                }
                else
                    mSelectedIds.clear();
                // FIXME: case when selectedIds was restored, but list currently not loaded

                mHasSelectionData.setValue(mSelectedIds.size() > 0);
                mItemsLiveData.setValue(list);
            } finally {
                addSelectionObserver();
            }
        });
    }

    public LiveData<List<ItemViewModel>> getItemsData() {
        return mItemsLiveData;
    }

    public LiveData<Boolean> hasSelectedItems() {
        return mHasSelectionData;
    }

    public void deselectAll() {
        List<ItemViewModel> list = mItemsLiveData.getValue();
        if (list != null)
            for (ItemViewModel item : list)
                item.setIsSelected(false);
    }

    public int getSelectedItemsCount() {
        return mSelectedIds.size();
    }

    public void saveState(Bundle outState) {
        outState.putParcelable("ListViewModelState", new SavedState(this));
    }

    public void restoreState(Bundle state) {
        SavedState s = state.getParcelable("ListViewModelState");
        if (s != null) {
            s.restore(this);
        }
    }

    private static class SavedState implements Parcelable {
        private HashSet<Long> selectedIds;

        SavedState(ListViewModel model) {
            selectedIds = new HashSet<>(model.mSelectedIds);
        }

        SavedState(Parcel in) {
            selectedIds = new HashSet<>();
            int cnt = in.readInt();
            for (int i = 0; i < cnt; i++)
                selectedIds.add(in.readLong());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(selectedIds.size());
            for (long id : selectedIds)
                dest.writeLong(id);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        void restore(ListViewModel model) {
            model.mSelectedIds.clear();
            model.mSelectedIds.addAll(selectedIds);
            model.mHasSelectionData.setValue(selectedIds.size() > 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
