package mvasoft.recyclerbinding.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ListViewModel {

    private final MutableLiveData<List<ItemViewModel>> mItemsLiveData;

    public ListViewModel() {
        mItemsLiveData = new MutableLiveData<>();
    }    
    
    public void setItemsList(List<ItemViewModel> list) {
        if (list != null) {
            HashSet<Long> wasSelected = getSelectedIds();
            for (ItemViewModel item : list)
                if (wasSelected.contains(item.getId()))
                    item.setIsSelected(true);
        }
        mItemsLiveData.postValue(list);
    }


    public LiveData<List<ItemViewModel>> getItemsData() {
        return mItemsLiveData;
    }

    public void deselectAll() {
        if (mItemsLiveData.getValue() != null)
            for (ItemViewModel item : mItemsLiveData.getValue())
                item.setIsSelected(false);
    }
    
    private HashSet<Long> getSelectedIds() {
        HashSet<Long> ids = new HashSet<>();
        for (ItemViewModel item : getSelectedItems())
            ids.add(item.getId());
        return ids;
    }
    
    public Iterable<ItemViewModel> getSelectedItems() {
        return new Iterable<ItemViewModel>() {
            @NonNull
            @Override
            public Iterator<ItemViewModel> iterator() {
                return new SelectedItemsIter(mItemsLiveData.getValue());
            }
        };
    }

    public int getSelectedItemsCount() {
        int res = 0;
        if (mItemsLiveData.getValue() != null)
            for (ItemViewModel item : mItemsLiveData.getValue())
                if (item.isSelected())
                    res++;
        return res;
    }

    private static class SelectedItemsIter implements Iterator<ItemViewModel> {
        private final List<? extends ItemViewModel> mList;
        int mCurrent = -1;

        SelectedItemsIter(@Nullable List<?extends ItemViewModel> list) {
            mList = list;
        }

        @Override
        public boolean hasNext() {
            if (mList != null)
                for (int i = mCurrent + 1; i < mList.size(); i++) {
                    Boolean isSelected = mList.get(i).getIsSelected().getValue();
                    if (isSelected != null && isSelected) {
                        mCurrent = i;
                        return true;
                    }
                }

            return false;
        }

        @Override
        public ItemViewModel next() {
            return mList != null ? mList.get(mCurrent) : null;
        }
    }
}
