package mvasoft.timetracker.ui;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import mvasoft.timetracker.R;
import mvasoft.timetracker.events.SnackbarEvent;
import mvasoft.timetracker.databinding.ActivityNavigationDrawerBinding;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.common.FabProvider;
import mvasoft.timetracker.ui.common.NavigationController;

public class NavigationDrawerActivity
        extends BindingSupportActivity<ActivityNavigationDrawerBinding, BaseViewModel>
        implements NavigationView.OnNavigationItemSelectedListener, FabProvider, HasSupportFragmentInjector {

    @Inject
    NavigationController navigationController;
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
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, getBinding().drawerLayout,
                getBinding().mainContent.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        getBinding().drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getBinding().navView.setNavigationItemSelectedListener(this);

        getBinding().mainContent.fab.setVisibility(View.GONE);
        getBinding().mainContent.fab.setOnClickListener(v -> {
            if (mFabListener != null)
                mFabListener.onClick(v);
        });

        toggle.setToolbarNavigationClickListener((v) -> {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0)
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            else
                getBinding().drawerLayout.openDrawer(GravityCompat.START);
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0)
                toggle.setDrawerIndicatorEnabled(true);
            else
                toggle.setDrawerIndicatorEnabled(false);
        });

        if (savedInstanceState == null && getBinding().navView.getMenu().size() > 0) {
            MenuItem menuItem = getBinding().navView.getMenu().getItem(0);
            getBinding().navView.setCheckedItem(menuItem.getItemId());
            onNavigationItemSelected(menuItem);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_sessions:
                navigationController.navigateToSessions();
                break;
            case R.id.nav_dates:
                navigationController.navigateToDates();
                break;
            case R.id.nav_settings:
                navigationController.navigateToSettings();
                break;
            case R.id.nav_backup:
                navigationController.navigateToBackup();
                break;
            default:
                return false;
        }

        getBinding().drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getBinding().drawerLayout.isDrawerOpen(GravityCompat.START))
            getBinding().drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
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
