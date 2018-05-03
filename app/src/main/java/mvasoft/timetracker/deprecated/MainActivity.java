package mvasoft.timetracker.deprecated;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.ActivityMainBinding;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.timetracker.ui.editsession.view.SessionEditFragment;
import mvasoft.timetracker.ui.extlist.view.TabbedActivity;

public class MainActivity extends BindingSupportActivity<ActivityMainBinding, BaseViewModel>
        implements NavigationView.OnNavigationItemSelectedListener,
        SessionListFragment.ISessionListCallbacks,
        FragmentManager.OnBackStackChangedListener {

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_session_list_tabbed:
                Intent intent = new Intent(this, TabbedActivity.class);
                startActivity(intent);
                break;

        }

        // Handle navigation view item clicks here.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackStackChanged() {
        updateHomeButton();
    }

    private void updateHomeButton() {
        int cnt = getSupportFragmentManager().getBackStackEntryCount();
        if (cnt < 1) {
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        else {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = getBinding().appbar.toolbar;//findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = getBinding().drawerLayout;// findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        NavigationView navigationView = getBinding().navView;// findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(this);
        Fragment fragment = fm.findFragmentById(R.id.fragment_holder);
        if (fragment == null) {
            fragment = SessionListFragment.newInstance();
            fm.beginTransaction().
                    add(R.id.fragment_holder, fragment).
                    commit();
        }
        updateHomeButton();
    }

    @Override
    protected BaseViewModel onCreateViewModel() {
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getModelVariableId() {
        return 0;
    }

    public void editSession(long sessionId) {
        final String EDIT_FRAGMENT_TAG = "EDIT_FRAGMENT_TAG";

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = SessionEditFragment.newInstance(sessionId);

        fm.beginTransaction().
                replace(R.id.fragment_holder, fragment).
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                addToBackStack(EDIT_FRAGMENT_TAG).
                commit();
    }

}
