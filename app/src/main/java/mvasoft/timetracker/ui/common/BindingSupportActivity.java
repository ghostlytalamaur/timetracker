package mvasoft.timetracker.ui.common;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

import org.greenrobot.eventbus.EventBus;

import dagger.android.support.DaggerAppCompatActivity;

public abstract class BindingSupportActivity<Binding extends ViewDataBinding,
        ViewModel extends BaseViewModel> extends EventBusSupportActivity {

    private Binding mBinding;
    private ViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, getLayoutId());
        if (getViewModel() != null)
            mBinding.setVariable(getModelVariableId(), getViewModel());
        bindVariables();
        mBinding.setLifecycleOwner(this);
        mBinding.executePendingBindings();
    }

    protected void bindVariables() {

    }

    protected Binding getBinding() {
        return mBinding;
    }

    protected ViewModel getViewModel() {
        if (mViewModel == null) {
            mViewModel = onCreateViewModel();
            if (mViewModel != null) {
                getLifecycle().addObserver(mViewModel);
            }

        }
        return mViewModel;
    }

    protected ViewModel onCreateViewModel() {
        return null;
    }

    protected @IdRes
    int getModelVariableId() {
        return 0;
    }

    protected abstract @LayoutRes
    int getLayoutId();

}
