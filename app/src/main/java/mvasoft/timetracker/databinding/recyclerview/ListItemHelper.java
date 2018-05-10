package mvasoft.timetracker.databinding.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListItemHelper {

    public static List<Long> getSelectedItemsIds(@Nullable List<? extends BaseItemModel> list) {
        ArrayList<Long> ids = new ArrayList<>();
        if (list != null)
            for (BaseItemModel item : list)
                if (item.getIsSelected())
                    ids.add(item.getId());
        return ids;
    }

    public static Iterable<BaseItemModel> getSelectedItemsIter(@Nullable final List<? extends BaseItemModel> list) {
        return new Iterable<BaseItemModel>() {
            @NonNull
            @Override
            public Iterator<BaseItemModel> iterator() {
                return new SelectedItemsIter(list);
            }
        };
    }

    public static void deselectAll(List<? extends BaseItemModel> list) {
        if (list != null)
            for (BaseItemModel item : list)
                if (item.getIsSelected())
                    item.setIsSelected(false);
    }

    private static class SelectedItemsIter implements Iterator<BaseItemModel> {
        private final List<? extends BaseItemModel> mList;
        int mCurrent = -1;

        SelectedItemsIter(@Nullable List<?extends BaseItemModel> list) {
            mList = list;
        }

        @Override
        public boolean hasNext() {
            if (mList != null)
                for (int i = mCurrent + 1; i < mList.size(); i++)
                    if (mList.get(i).getIsSelected()) {
                        mCurrent = i;
                        return true;
                    }

            return false;
        }

        @Override
        public BaseItemModel next() {
            return mList != null ? mList.get(mCurrent) : null;
        }
    }
}
