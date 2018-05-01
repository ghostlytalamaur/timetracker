package mvasoft.timetracker.databinding.recyclerview;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.drextended.actionhandler.listener.ActionClickListener;
import com.drextended.rvdatabinding.adapter.BindingHolder;
import com.drextended.rvdatabinding.delegate.ModelItemDelegate;

import java.util.List;

import mvasoft.timetracker.core.IdProvider;

public class ModelItemIdDelegate<T> extends ModelItemDelegate<T> {

    private ActionClickListener mActionHandler;
    private int mActionHandlerId;

    public ModelItemIdDelegate(ActionClickListener actionHandler, @NonNull Class<? extends T> modelClass, int itemLayoutResId, int modelId, int actionHandlerId) {
        super(modelClass, itemLayoutResId, modelId);
        mActionHandler = actionHandler;
        mActionHandlerId = actionHandlerId;
    }

    @Override
    public long getItemId(List<T> items, int position) {
        T item = items.get(position);
        if (item instanceof IdProvider)
            return  ((IdProvider) item).getId();
        else
            return super.getItemId(items, position);
    }

    private ActionClickListener getActionHandler() {
        return mActionHandler;
    }

    @NonNull
    @Override
    public BindingHolder<ViewDataBinding> onCreateViewHolder(ViewGroup parent) {
        BindingHolder<ViewDataBinding> holder = super.onCreateViewHolder(parent);
        final ActionClickListener actionHandler = getActionHandler();
        if (actionHandler != null) {
            holder.getBinding().setVariable(mActionHandlerId, actionHandler);
        }
        return holder;
    }
}
