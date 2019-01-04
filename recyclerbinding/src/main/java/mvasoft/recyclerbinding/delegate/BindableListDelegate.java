package mvasoft.recyclerbinding.delegate;

import androidx.lifecycle.LifecycleOwner;
import androidx.databinding.ViewDataBinding;
import androidx.annotation.NonNull;

import com.drextended.actionhandler.listener.ActionClickListener;

import java.util.List;

import mvasoft.recyclerbinding.adapter.BindableHolder;
import mvasoft.recyclerbinding.viewmodel.ItemViewModel;
import mvasoft.recyclerbinding.viewmodel.ListViewModel;

public class BindableListDelegate<VB extends ViewDataBinding>
        extends BindableDelegate<ItemViewModel, VB>  {

    private final int mModelVariableId;
    private final int mItemModelVariableId;
    private final Class<? extends ItemViewModel> mClass;
    private ActionClickListener mActionHandler;
    private int mActionHandlerId;
    private final ListViewModel mItemsModel;

    public BindableListDelegate(LifecycleOwner lifecycleOwner, ListViewModel itemsModel, int layoutRes, int modelVariableId,
                                int itemModelVariableId,
                                Class<? extends ItemViewModel> itemViewModelClass) {
        super(lifecycleOwner, layoutRes);
        mModelVariableId = modelVariableId;
        mItemModelVariableId = itemModelVariableId;
        mClass = itemViewModelClass;
        mItemsModel = itemsModel;
    }

    @Override
    protected void onBindVariables(BindableHolder<VB> bindableHolder, @NonNull List<ItemViewModel> items, int position) {
        ItemViewModel item = items.get(position);
//        List<ItemViewModel> list = item.getItemsData().getValue();
        bindableHolder.getBinding().setVariable(mModelVariableId, mItemsModel);
        bindableHolder.getBinding().setVariable(mItemModelVariableId, item);
        if (mActionHandler != null && mActionHandlerId > 0)
            bindableHolder.getBinding().setVariable(mActionHandlerId, mActionHandler);
    }

    @Override
    protected boolean isForViewType(@NonNull List<ItemViewModel> items, int position) {
        ItemViewModel item = items.get(position);
//        List<ItemViewModel> list = items.getItemsData().getValue();
        return mClass.isAssignableFrom(item.getClass());
    }

    public void setActionHandler(int variableId, ActionClickListener handler) {
        mActionHandlerId = variableId;
        mActionHandler = handler;
    }
}
