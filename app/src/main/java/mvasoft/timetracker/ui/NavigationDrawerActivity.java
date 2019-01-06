package mvasoft.timetracker.ui;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.ActivityNavigationDrawerBinding;
import mvasoft.timetracker.events.SnackbarEvent;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.common.FabProvider;

public class NavigationDrawerActivity
        extends BindingSupportActivity<ActivityNavigationDrawerBinding, BaseViewModel>
        implements FabProvider, HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    private View.OnClickListener mFabListener;

    @Override
    public void setImageResource(int id) {
        Drawable drawable = getResources().getDrawable(id, null);
        getBinding().mainContent.fab.setImageDrawable(drawable);
        if (drawable instanceof Animatable)
            ((Animatable) drawable).start();
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE)
            getBinding().mainContent.fab.show();
        else
            getBinding().mainContent.fab.hide();
    }

    @Override
    public void removeClickListener(FloatingActionButton.OnClickListener listener) {
        mFabListener = null;
    }

    @Override
    public void setClickListener(FloatingActionButton.OnClickListener listener) {
        mFabListener = listener;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(getBinding().mainContent.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_fragment);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setDrawerLayout(getBinding().drawerLayout)
                .build();
        NavigationUI.setupWithNavController(getBinding().mainContent.toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(getBinding().navView, navController);

        getBinding().mainContent.fab.setVisibility(View.GONE);
        getBinding().mainContent.fab.setOnClickListener(v -> {
            if (mFabListener != null)
                mFabListener.onClick(v);
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_navigation_drawer;
    }

    @Override
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSnackbarEvent(SnackbarEvent e) {
        Snackbar.make(getBinding().mainContent.fab, e.message, e.duration).show();
    }

}
