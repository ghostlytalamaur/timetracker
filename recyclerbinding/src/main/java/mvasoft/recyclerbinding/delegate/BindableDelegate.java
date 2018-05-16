package mvasoft.recyclerbinding.delegate;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;

import java.lang.ref.WeakReference;
import java.util.List;

import mvasoft.recyclerbinding.adapter.BindableHolder;

public abstract class BindableDelegate<T, VB extends ViewDataBinding> extends AdapterDelegate<T> {

    @LayoutRes
    private int mLayoutId;
    private WeakReference<LifecycleOwner> mLifecycleOwner;

    BindableDelegate(LifecycleOwner lifecycleOwner, @LayoutRes int layoutRes) {
        mLayoutId = layoutRes;
        mLifecycleOwner = new WeakReference<>(lifecycleOwner);
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        BindableHolder vh = BindableHolder.newInstance(mLayoutId, LayoutInflater.from(parent.getContext()),
                parent, false);
        vh.getBinding().setLifecycleOwner(mLifecycleOwner.get());
        return vh;
    }

    @Override
    protected void onBindViewHolder(@NonNull T items, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        //noinspection unchecked
        BindableHolder<VB> bindableHolder = (BindableHolder<VB>) holder;
        onBindVariables(bindableHolder, items, position);
        bindableHolder.getBinding().executePendingBindings();
    }

    @SuppressWarnings("WeakerAccess")
    abstract protected void onBindVariables(BindableHolder<VB> bindableHolder, @NonNull T items, int position);
}
