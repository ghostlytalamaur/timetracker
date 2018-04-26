package mvasoft.timetracker.ui.base;

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

        if (mViewModel == null) {
            mViewModel = onCreateViewModel();
        }

        mBinding.setVariable(getModelVariableId(), mViewModel);
        mBinding.executePendingBindings();
        return mBinding.getRoot();
    }

    protected abstract ViewModel onCreateViewModel();

    protected Binding getBinding() {
        return mBinding;
    }

    protected abstract @LayoutRes int getLayoutId();
    protected abstract @IdRes int getModelVariableId();
}
