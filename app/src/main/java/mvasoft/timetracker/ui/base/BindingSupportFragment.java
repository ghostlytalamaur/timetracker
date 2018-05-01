package mvasoft.timetracker.ui.base;

import android.arch.lifecycle.LifecycleObserver;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BindingSupportFragment<Binding extends ViewDataBinding,
        ViewModel extends BaseViewModel> extends Fragment {

    private Binding mBinding;
    private ViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);

        if (getViewModel() != null)
            mBinding.setVariable(getModelVariableId(), getViewModel());
        mBinding.setLifecycleOwner(this);
        mBinding.executePendingBindings();
        return mBinding.getRoot();
    }

    protected ViewModel getViewModel() {
        if (mViewModel == null) {
            mViewModel = onCreateViewModel();
            if (mViewModel instanceof LifecycleObserver)
                getLifecycle().addObserver((LifecycleObserver) mViewModel);
        }
        return mViewModel;
    }

    protected Binding getBinding() {
        return mBinding;
    }


    protected ViewModel onCreateViewModel() {
        return null;
    }

    protected @IdRes int getModelVariableId() {
        return 0;
    }
    protected abstract @LayoutRes int getLayoutId();
}
