package mvasoft.recyclerbinding.adapter;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Recycler View Holder to use with data mBinding
 *
 * @param <VB> The type of view data binding
 */
public class BindableHolder<VB extends ViewDataBinding> extends RecyclerView.ViewHolder {

    /**
     * View data binding of this holder
     */
    private VB mBinding;

    /**
     * Creates new View Holder from provided layout
     *
     * @param layoutId       The layout resource ID of the layout to inflate.
     * @param inflater       The LayoutInflater used to inflate the binding layout.
     * @param parent         Optional view to be the parent of the generated hierarchy
     * @param attachToParent Whether the inflated hierarchy should be attached to the
     *                       parent parameter. If false, parent is only used to create
     *                       the correct subclass of LayoutParams for the root view in the XML.
     * @param <VB>           The type of view data binding
     * @return The newly-created view-holder for the binding with inflated layout.
     */
    public static <VB extends ViewDataBinding> BindableHolder<VB> newInstance(
            @LayoutRes int layoutId, LayoutInflater inflater,
            @Nullable ViewGroup parent, boolean attachToParent) {

        final VB vb = DataBindingUtil.inflate(inflater, layoutId, parent, attachToParent);
        return new BindableHolder<>(vb);
    }

    /**
     * Creates new View Holder from provided binding
     *
     * @param binding The view data binding class for this view-holder
     */
    private BindableHolder(VB binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    /**
     * Returns view data binding of this holder
     *
     * @return view data binding of this holder
     */
    public VB getBinding() {
        return mBinding;
    }

}