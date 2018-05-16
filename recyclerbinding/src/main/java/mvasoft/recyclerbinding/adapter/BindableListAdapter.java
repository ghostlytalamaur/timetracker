package mvasoft.recyclerbinding.adapter;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import com.hannesdorfmann.adapterdelegates3.AbsDelegationAdapter;
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;

import java.util.List;

import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.recyclerbinding.viewmodel.ListViewModel;

public class BindableListAdapter extends AbsDelegationAdapter<List<ItemViewModel>> {

    private final AsyncListDiffer<ItemViewModel> mDiffer;
    private final ListViewModel mViewModel;

    @SuppressWarnings("unchecked")
    public BindableListAdapter(@NonNull LifecycleOwner owner, @NonNull ListViewModel viewModel,
                               AdapterDelegate<List<ItemViewModel>>... delegates) {

        for (AdapterDelegate<List<ItemViewModel>> delegate : delegates)
            delegatesManager.addDelegate(delegate);
        mDiffer = new AsyncListDiffer<>(this, new DifferCallback());

        mViewModel = viewModel;
        mViewModel.getItemsData().observe(owner, new Observer<List<ItemViewModel>>() {
            @Override
            public void onChanged(@Nullable List<ItemViewModel> itemViewModels) {
                mDiffer.submitList(itemViewModels);
            }
        });
        mDiffer.submitList(mViewModel.getItemsData().getValue());
    }

    @Override
    public void setItems(List<ItemViewModel> items) {
        throw new RuntimeException("BindableListAdapter.setItems(). Use ListViewMode.getItems() to set new items list.");
    }

    @Override
    public int getItemCount() {
        if (items != mDiffer.getCurrentList())
            items = mDiffer.getCurrentList();

        return mDiffer.getCurrentList().size();
    }

    @Override
    public long getItemId(int position) {
        if (items != mDiffer.getCurrentList())
            items = mDiffer.getCurrentList();

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

    @Override
    public List<ItemViewModel> getItems() {
        return mDiffer.getCurrentList();
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(getItems(), position, holder, null);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        delegatesManager.onBindViewHolder(getItems(), position, holder, payloads);
    }

    @Override public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(getItems(), position);
    }

}
