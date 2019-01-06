package mvasoft.recyclerbinding;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class DiffUtilItemViewModelCallback extends DiffUtil.ItemCallback<ItemViewModel> {
    @Override
    public boolean areItemsTheSame(@NonNull ItemViewModel oldItem, @NonNull ItemViewModel newItem) {
        return oldItem == newItem;
    }

    @Override
    public boolean areContentsTheSame(@NonNull ItemViewModel oldItem, @NonNull ItemViewModel newItem) {
        return oldItem.equals(newItem);
    }
}
