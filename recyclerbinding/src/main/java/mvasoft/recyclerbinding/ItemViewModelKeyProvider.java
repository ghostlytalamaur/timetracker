package mvasoft.recyclerbinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;
import mvasoft.recyclerbinding.ItemViewModel;

public class ItemViewModelKeyProvider extends ItemKeyProvider<Long> {

    private ItemsProvider mItemsProvider;
    private List<ItemViewModel> mItems;
    private LongSparseArray<Integer> mId2Pos;

    /**
     * Creates a new provider with the given scope.
     *
     * @param scope Scope can't be changed at runtime.
     */
    public ItemViewModelKeyProvider(ItemsProvider itemsProvider, int scope) {
        super(scope);
        mItemsProvider = itemsProvider;
        mId2Pos = new LongSparseArray<>();
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        updateItems();
        if (mItems != null)
            return mItems.get(position).getId();
        else
            return null;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        return mId2Pos.get(key, RecyclerView.NO_POSITION);
    }

    private void updateItems() {
        if (mItems == mItemsProvider.getItems())
            return;

        mItems = mItemsProvider.getItems();
        mId2Pos.clear();
        if (mItems != null)
            for (int i = 0; i < mItems.size(); i++) {
                mId2Pos.put(mItems.get(i).getId(), i);
            }

    }

    public interface ItemsProvider {
        List<ItemViewModel> getItems();
    }
}
