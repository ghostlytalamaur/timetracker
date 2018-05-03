package mvasoft.timetracker.databinding.recyclerview;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;

import com.drextended.rvdatabinding.adapter.BaseBindableAdapter;
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;

import java.util.List;

public class LiveBindableAdapter<ListModel extends List<BaseItemModel>> extends
        BaseBindableAdapter<ListModel> {

    private LiveData<ListModel> mData;
    private AsyncListDiffer<BaseItemModel> mDiffer;


    public LiveBindableAdapter(AdapterDelegate<ListModel>... adapterDelegates) {
        super(adapterDelegates);
//        ListDifferCallback diffCallback = new ListDifferCallback();
        mDiffer = new AsyncListDiffer<>(this, new ItemDifferCallback());
    }

    public void setData(@NonNull LifecycleOwner owner, LiveData<ListModel> data) {
            mData = data;
            if (mData != null)
                mData.observe(owner, new Observer<ListModel>() {
                    @Override
                    public void onChanged(@Nullable ListModel data) {
                        mDiffer.submitList(data);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    @Override
    public ListModel getItems() {
        return (ListModel) mDiffer.getCurrentList();
    }

    private class ItemDifferCallback extends DiffUtil.ItemCallback<BaseItemModel> {

        @Override
        public boolean areItemsTheSame(BaseItemModel oldItem, BaseItemModel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(BaseItemModel oldItem, BaseItemModel newItem) {
            return oldItem.equals(newItem);
        }
    }
}
