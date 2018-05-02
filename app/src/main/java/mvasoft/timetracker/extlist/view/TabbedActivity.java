package mvasoft.timetracker.extlist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.GroupType;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.ActivityTabbedBinding;
import mvasoft.timetracker.editsession.view.EditSessionActivity;
import mvasoft.timetracker.extlist.modelview.TabbedActivityViewModel;
import mvasoft.timetracker.ui.base.BindingSupportActivity;

public class TabbedActivity extends BindingSupportActivity<ActivityTabbedBinding,
        TabbedActivityViewModel> implements ExSessionListFragment.ISessionListCallbacks {

    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(getBinding().toolbar);
        initViewPager();

    }

    @Override
    protected TabbedActivityViewModel onCreateViewModel() {
        return ViewModelProviders.of(this).get(TabbedActivityViewModel.class);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_tabbed;
    }

    @Override
    protected int getModelVariableId() {
        return BR.view_model;
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        mActionMode = mode;
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        mActionMode = null;
        super.onSupportActionModeFinished(mode);
    }

    private void initViewPager() {
        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            private GroupType posToGroupType(int position) {
                if (position >= 0 && position < GroupType.values().length)
                    return GroupType.values()[position];
                else
                    return GroupType.gt_None;

            }

            @Override
            public Fragment getItem(int position) {
                return ExSessionListFragment.newInstance(posToGroupType(position));
            }

            @Override
            public int getCount() {
                return GroupType.values().length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return getResources().getStringArray(R.array.group_types)[position];
            }
        };

        getBinding().viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mActionMode == null)
                    return;

                if (mActionMode.getTag() == null || !mActionMode.getTag().equals(GroupType.values()[position]))
                    mActionMode.finish();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        getBinding().viewPager.setAdapter(adapter);
        getBinding().tabLayout.setupWithViewPager(getBinding().viewPager);
    }

    public void editSession(long sessionId) {
        Intent intent = new Intent(this, EditSessionActivity.class);
        intent.putExtras(EditSessionActivity.makeArgs(sessionId));
        startActivity(intent);
    }

}
