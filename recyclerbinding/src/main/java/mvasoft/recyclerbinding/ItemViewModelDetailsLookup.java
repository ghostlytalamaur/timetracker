package mvasoft.recyclerbinding;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

public class ItemViewModelDetailsLookup extends ItemDetailsLookup<Long> {

    private final RecyclerView mRecyclerView;
    private final ItemKeyProvider<Long> mKeyProvider;

    public ItemViewModelDetailsLookup(RecyclerView recyclerView, ItemKeyProvider<Long> keyProvider) {
        mRecyclerView = recyclerView;
        mKeyProvider = keyProvider;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
            if (holder != null) {
                int pos = holder.getAdapterPosition();
                long key = mKeyProvider.getKey(pos);
                return new ItemViewModelDetails(pos, key);
            }
        }
        return null;
    }

    static class ItemViewModelDetails extends ItemDetails<Long> {

        private final int mPos;
        private final long mKey;

        ItemViewModelDetails(int pos, long key) {
            mPos = pos;
            mKey = key;
        }

        @Override
        public int getPosition() {
            return mPos;
        }

        @Nullable
        @Override
        public Long getSelectionKey() {
            return mKey;
        }
    }
}
