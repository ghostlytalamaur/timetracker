package mvasoft.timetracker.ui;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import mvasoft.timetracker.R;
import mvasoft.timetracker.events.SnackbarEvent;
import mvasoft.timetracker.databinding.ActivityNavigationDrawerBinding;
import mvasoft.timetracker.ui.backup.BackupFragment;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.common.FabProvider;
import mvasoft.timetracker.ui.common.FragmentFactory;
import mvasoft.timetracker.ui.common.NavigationController;
import mvasoft.timetracker.ui.editdate.DatesViewFragment;
import mvasoft.timetracker.ui.extlist.ExSessionListFragment;
import mvasoft.timetracker.ui.preferences.PreferencesFragment;

public class NavigationDrawerActivity
        extends BindingSupportActivity<ActivityNavigationDrawerBinding, BaseViewModel>
        implements NavigationView.OnNavigationItemSelectedListener, FabProvider, NavigationController {

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
    public void showFragment(FragmentFactory factory) {
        Fragment fragment = factory.createFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
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

        if (savedInstanceState == null && getBinding().navView.getMenu().size() > 0) {
            MenuItem menuItem = getBinding().navView.getMenu().getItem(0);
            getBinding().navView.setCheckedItem(menuItem.getItemId());
            onNavigationItemSelected(menuItem);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_sessions: {
                if (!(fm.findFragmentById(R.id.content_frame) instanceof ExSessionListFragment)) {
                    long today = System.currentTimeMillis() / 1000;
                    fragment = ExSessionListFragment.newInstance(today, today);
                }
                break;
            }
            case R.id.nav_dates: {
                if (!(fm.findFragmentById(R.id.content_frame) instanceof DatesViewFragment)) {
                    fragment = new DatesViewFragment();
                }
                break;
            }
            case R.id.nav_settings: {
                if (!(fm.findFragmentById(R.id.content_frame) instanceof PreferencesFragment))
                    fragment = new PreferencesFragment();
                break;
            }
            case R.id.nav_backup:
                if (!(fm.findFragmentById(R.id.content_frame) instanceof BackupFragment))
                    fragment = new BackupFragment();
                break;
            default:
                return false;
        }

        if (fragment != null) {
            FragmentTransaction transaction = fm.beginTransaction();
            if (getSupportFragmentManager().findFragmentById(R.id.content_frame) != null)
                    transaction.addToBackStack(null);
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();
        }

        getBinding().drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getBinding().drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
