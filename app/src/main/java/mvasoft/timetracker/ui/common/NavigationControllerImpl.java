package mvasoft.timetracker.ui.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import javax.inject.Inject;

import mvasoft.timetracker.R;
import mvasoft.timetracker.ui.NavigationDrawerActivity;
import mvasoft.timetracker.ui.backup.BackupFragment;
import mvasoft.timetracker.ui.editdate.DatesViewFragment;
import mvasoft.timetracker.ui.editdate.EditDateFragment;
import mvasoft.timetracker.ui.editsession.EditSessionFragment;
import mvasoft.timetracker.ui.extlist.ExSessionListFragment;
import mvasoft.timetracker.ui.preferences.PreferencesFragment;
import mvasoft.timetracker.vo.SessionsGroup;

public class NavigationControllerImpl implements NavigationController {

    private final int mContainerId;
    private final FragmentManager mFragmentManager;

    @Inject
    public NavigationControllerImpl(NavigationDrawerActivity activity) {
        mContainerId = R.id.content_frame;
        mFragmentManager = activity.getSupportFragmentManager();
    }

    @Override
    public void editSession(long sessionId) {
        showFragment(EditSessionFragment.class, () -> EditSessionFragment.newInstance(sessionId), true);
    }

    @Override
    public void editDate(long unixTime) {
        showFragment(EditDateFragment.class, () -> EditDateFragment.makeInstance(unixTime), true);
    }

    @Override
    public void navigateToSessions() {
        showFragment(ExSessionListFragment.class, () -> {
            long today = System.currentTimeMillis() / 1000;
            return ExSessionListFragment.newInstance(SessionsGroup.GroupType.gtDay, today, today);
        }, false);
    }

    @Override
    public void navigateToDates() {
        showFragment(DatesViewFragment.class, DatesViewFragment::new, false);
    }

    @Override
    public void navigateToSettings() {
        showFragment(PreferencesFragment.class, PreferencesFragment::new, false);
    }

    @Override
    public void navigateToBackup() {
        showFragment(BackupFragment.class, BackupFragment::new, false);

    }

    private <F extends Fragment> void showFragment(Class<F> fragmentClass, Factory<F> factory, boolean addToBackStack) {
        Fragment curFragment = mFragmentManager.findFragmentById(R.id.content_frame);
        if (fragmentClass.isInstance(curFragment)) {
            return;
        }

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (addToBackStack)
            ft.addToBackStack(null);
        else
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(mContainerId, factory.create());
        ft.commit();
    }

    interface Factory<F extends Fragment> {
        F create();
    }
}
