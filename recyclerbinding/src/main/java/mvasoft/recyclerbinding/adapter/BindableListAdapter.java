package mvasoft.recyclerbinding.adapter;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;

import com.hannesdorfmann.adapterdelegates3.AbsDelegationAdapter;
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;

import java.util.List;

import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.recyclerbinding.viewmodel.ListViewModel;

public class BindableListAdapter extends AbsDelegationAdapter<ListViewModel> {

    private final AsyncListDiffer<ItemViewModel> mDiffer;

    @SuppressWarnings("unchecked")
    public BindableListAdapter(@NonNull LifecycleOwner owner, @NonNull ListViewModel viewModel,
                               AdapterDelegate<ListViewModel>... delegates) {

        for (AdapterDelegate<ListViewModel> delegate : delegates)
            delegatesManager.addDelegate(delegate);
        mDiffer = new AsyncListDiffer<>(this, new DifferCallback());
        setItems(owner, viewModel);
    }

    private void setItems(@NonNull LifecycleOwner owner, ListViewModel items) {
        super.setItems(items);
        items.getItemsData().observe(owner, new Observer<List<ItemViewModel>>() {
            @Override
            public void onChanged(@Nullable List<ItemViewModel> itemViewModels) {
                mDiffer.submitList(itemViewModels);
            }
        });
        mDiffer.submitList(items.getItemsData().getValue());
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    @Override
    public long getItemId(int position) {
        return mDiffer.getCurrentList().get(position).getId();
    }

    static class DifferCallback extends DiffUtil.ItemCallback<ItemViewModel> {
        @Override
        public boolean areItemsTheSame(ItemViewModel oldItem, ItemViewModel newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(ItemViewModel oldItem, ItemViewModel newItem) {
            return oldItem.equals(newItem);
        }

    }

}
