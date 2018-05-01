package mvasoft.timetracker.databinding.recyclerview;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.drextended.rvdatabinding.adapter.BindableAdapter;
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;

import java.util.List;

public class LiveBindableAdapter<ListModel extends List, T extends LiveData<ListModel>> extends
        BindableAdapter<ListModel> {

        private LiveData<ListModel> mData;

    public LiveBindableAdapter(AdapterDelegate<ListModel>... adapterDelegates) {
        super(adapterDelegates);
    }

    public void setData(@NonNull LifecycleOwner owner, T data) {
            mData = data;
            if (mData != null)
                mData.observe(owner, new Observer<ListModel>() {
                    @Override
                    public void onChanged(@Nullable ListModel data) {
                        setItems(data);
                        notifyDataSetChanged();
                    }
                });
        }

}
