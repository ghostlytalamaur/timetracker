package mvasoft.recyclerbinding.delegate;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;

import com.drextended.actionhandler.listener.ActionClickListener;

import java.util.List;

import mvasoft.recyclerbinding.adapter.BindableHolder;

public class BindableListDelegate<T, VB extends ViewDataBinding>
        extends BindableDelegate<List<T>, VB>  {

    private final int mModelVariableId;
    private final Class<? extends T> mClass;
    private ActionClickListener mActionHandler;
    private int mActionHandlerId;

    public BindableListDelegate(LifecycleOwner lifecycleOwner, int layoutRes, int modelVariableId,
                                Class<? extends T> itemViewModelClass) {
        super(lifecycleOwner, layoutRes);
        mModelVariableId = modelVariableId;
        mClass = itemViewModelClass;
    }

    @Override
    protected void onBindVariables(BindableHolder<VB> bindableHolder, @NonNull List<T> items, int position) {
        bindableHolder.getBinding().setVariable(mModelVariableId, items.get(position));
        if (mActionHandler != null && mActionHandlerId > 0)
            bindableHolder.getBinding().setVariable(mActionHandlerId, mActionHandler);
    }

    @Override
    protected boolean isForViewType(@NonNull List<T> items, int position) {
        return mClass.isAssignableFrom(items.get(position).getClass());
    }

    public void setActionHandler(int variableId, ActionClickListener handler) {
        mActionHandlerId = variableId;
        mActionHandler = handler;
    }
}
