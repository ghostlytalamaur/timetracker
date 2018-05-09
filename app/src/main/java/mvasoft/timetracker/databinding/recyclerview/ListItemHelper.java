package mvasoft.timetracker.databinding.recyclerview;

import java.util.Iterator;
import java.util.List;

public class ListItemHelper {

    public static Iterator<Long> getSelectedItemIds(List<BaseItemModel> list) {
        return new Iterator<Long>() {
            int mCurrent = -1;

            @Override
            public boolean hasNext() {
                return (list != null) && (mCurrent < list.size());
            }

            @Override
            public Long next() {
                mCurrent++;
                return list.get(mCurrent).getId();
            }
        };
    }
}
