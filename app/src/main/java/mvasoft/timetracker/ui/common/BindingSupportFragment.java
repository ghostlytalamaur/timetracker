package mvasoft.timetracker.ui.common;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;

import androidx.fragment.app.Fragment;
import dagger.android.support.DaggerFragment;
import mvasoft.timetracker.core.TimeTrackerApp;

public abstract class BindingSupportFragment<Binding extends ViewDataBinding,
        ViewModel extends BaseViewModel> extends Fragment {

    private Binding mBinding;
    private ViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);

        if (getViewModel() != null)
            mBinding.setVariable(getModelVariableId(), getViewModel());
        mBinding.setLifecycleOwner(this);
        mBinding.executePendingBindings();
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (shouldRegisterToEventBus())
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (shouldRegisterToEventBus())
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    protected boolean shouldRegisterToEventBus() {
        // Note: registered fragment, that contains in FragmentStatePagerAdapter
        // will receive events even then when it not visible to user,
        return false;
    }

    protected ViewModel getViewModel() {
        if (mViewModel == null) {
            mViewModel = onCreateViewModel();
            if (mViewModel != null)
                getLifecycle().addObserver(mViewModel);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = TimeTrackerApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
