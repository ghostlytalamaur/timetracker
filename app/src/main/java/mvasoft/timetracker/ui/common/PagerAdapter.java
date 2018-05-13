package mvasoft.timetracker.ui.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

public abstract class PagerAdapter extends FragmentStatePagerAdapter {

    private final SparseArray<WeakReference<Fragment>> mFragments;

    protected PagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new SparseArray<>();
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        mFragments.put(position, new WeakReference<>(f));
        return f;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Nullable
    public Fragment getFragment(int position) {
        final WeakReference<Fragment> ref = mFragments.get(position);
        if (ref != null)
            return ref.get();
        else
            return null;
    }
}
