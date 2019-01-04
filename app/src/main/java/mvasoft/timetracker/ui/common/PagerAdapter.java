package mvasoft.timetracker.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class PagerAdapter extends FragmentStatePagerAdapterEx {

    private final SparseArray<WeakReference<Fragment>> mFragments;

    private final List<Long> mPreviousIds;
    private final List<Long> mCurrentIds;


    protected PagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new SparseArray<>();
        mPreviousIds = new ArrayList<>();
        mCurrentIds = new ArrayList<>();
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

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (!hasIds())
            return super.getItemPosition(object);

        if (mCurrentIds == null)
            return POSITION_NONE;

        long newId = getItemId(object);
        int newIdx = mCurrentIds.indexOf(newId);
        if (newIdx < 0)
            return POSITION_NONE;

        if (mPreviousIds != null && mPreviousIds.indexOf(newId) == newIdx)
            return POSITION_UNCHANGED;
        else
            return newIdx;
    }

    @Override
    public void notifyDataSetChanged() {
        if (hasIds()) {
            mCurrentIds.clear();
            for (int i = 0; i < getCount(); i++)
                mCurrentIds.add(getItemId(i));
        }

        super.notifyDataSetChanged();

        if (hasIds()) {
            mPreviousIds.clear();
            for (int i = 0; i < getCount(); i++)
                mPreviousIds.add(getItemId(i));
        }
    }

    protected long getItemId(@NonNull Object object) {
        return -1;
    }

    protected boolean hasIds() {
        return false;
    }
}
