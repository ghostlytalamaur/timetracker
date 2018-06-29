package mvasoft.recyclerbinding.delegate;

import android.arch.lifecycle.LifecycleOwner;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;

import com.drextended.actionhandler.listener.ActionClickListener;

import java.util.List;

import mvasoft.recyclerbinding.adapter.BindableHolder;
import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.recyclerbinding.viewmodel.ListViewModel;

public class BindableListDelegate<VB extends ViewDataBinding>
        extends BindableDelegate<ListViewModel, VB>  {

    private final int mModelVariableId;
    private final int mItemModelVariableId;
    private final Class<? extends ItemViewModel> mClass;
    private ActionClickListener mActionHandler;
    private int mActionHandlerId;

    public BindableListDelegate(LifecycleOwner lifecycleOwner, int layoutRes, int modelVariableId,
                                int itemModelVariableId,
                                Class<? extends ItemViewModel> itemViewModelClass) {
        super(lifecycleOwner, layoutRes);
        mModelVariableId = modelVariableId;
        mItemModelVariableId = itemModelVariableId;
        mClass = itemViewModelClass;
    }

    @Override
    protected void onBindVariables(BindableHolder<VB> bindableHolder, @NonNull ListViewModel items, int position) {
        List<ItemViewModel> list = items.getItemsData().getValue();
        bindableHolder.getBinding().setVariable(mModelVariableId, items);
        if ((list != null) && (position >= 0 && position < list.size()))
            bindableHolder.getBinding().setVariable(mItemModelVariableId, list.get(position));
        if (mActionHandler != null && mActionHandlerId > 0)
            bindableHolder.getBinding().setVariable(mActionHandlerId, mActionHandler);
    }

    @Override
    protected boolean isForViewType(@NonNull ListViewModel items, int position) {
        List<ItemViewModel> list = items.getItemsData().getValue();
        return (list != null) &&
                (position >= 0 && position < list.size()) &&
                mClass.isAssignableFrom(list.get(position).getClass());
    }

    public void setActionHandler(int variableId, ActionClickListener handler) {
        mActionHandlerId = variableId;
        mActionHandler = handler;
    }
}
