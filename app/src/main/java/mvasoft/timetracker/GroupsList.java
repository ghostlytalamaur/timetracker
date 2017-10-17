package mvasoft.timetracker;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashSet;

import mvasoft.timetracker.data.DatabaseDescription;
import mvasoft.timetracker.data.DatabaseDescription.GroupsDescription;

class GroupsList {

    private IGroupsChangesListener mListener;
    private ArrayList<SessionGroup> mList;
    private Cursor mCursor;

    GroupsList() {
        mList = new ArrayList<>();
    }


    // TODO: Refactoring for update changed groups and notification with modified/added/deleted/moved
    public void swapCursor(Cursor cursor) {
        boolean wasCursor = mCursor != null;
        if (wasCursor)
            mCursor.close();
        mCursor = cursor;
        if (wasCursor)
            updateData();
        else
            fillList();
    }

    private void notifyDataChanged() {
        if (mListener != null)
            mListener.onDataChanged();
    }

    private void notifyItemRemoved(int i) {
        if (mListener != null)
            mListener.onItemRemoved(i);
    }

    private void notifyItemChanged(int idx) {
        if (mListener != null)
            mListener.onItemChanged(idx);
    }

    private void notifyItemMoved(int idx, int i) {
        if (mListener != null)
            mListener.onItemMoved(idx, i);
    }

    public void setChangesListener(IGroupsChangesListener listener) {
        if (mListener != listener)
            mListener = listener;
    }

    private void updateData() {
        if (mCursor == null) {
            notifyDataChanged();
            return;
        }

        int idx;
        HashSet<Integer> processed = new HashSet<>();
        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            long id = mCursor.getLong(mCursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));

            idx = indexByID(id);
            long start = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_START));
            long end = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_END));
            long duration = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_DURATION));
            int cnt = mCursor.getInt(mCursor.getColumnIndex(GroupsDescription.COLUMN_UNCOMPLETED_COUNT));

            if (idx < 0) {
                mList.add(i, new SessionGroup(id, start, end, duration, cnt));
            }
            else if (!get(idx).sameData(start, end, duration, cnt)) {
                get(idx).updateData(start, end, duration, cnt);
                notifyItemChanged(idx);
                if (idx != i) {
                    mList.add(i, mList.remove(idx));
                    notifyItemMoved(idx, i);
                }
            }
            processed.add(i);
        }

        for (int i = 0; i < count(); i++)
            if (!processed.contains(i)) {
                mList.remove(i);
                notifyItemRemoved(i);
            }
    }

    private void fillList() {
        mList.clear();
        if (mCursor == null)
            return;

        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            // TODO: return minimal ID of session in group in content provider
            long id = mCursor.getLong(mCursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));
            long start = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_START));
            long end = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_END));
            long duration = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_DURATION));
            int cnt = mCursor.getInt(mCursor.getColumnIndex(GroupsDescription.COLUMN_UNCOMPLETED_COUNT));
            mList.add(new SessionGroup(id, start, end, duration, cnt));
        }
    }

    private int indexByID(long id) {
        for (int i = 0; i < count(); i++)
            if (get(i).mID == id)
                return i;
        return -1;
    }

    private SessionGroup getByID(long id) {
        for (SessionGroup g : mList)
            if (g.mID == id)
                return g;
        return null;
    }

    public int count() {
        return mList.size();
    }

    public SessionGroup get(int idx) {
        if ((idx >= 0) && (idx < mList.size()))
            return mList.get(idx);
        else
            return null;
    }

    public boolean hasOpenedSessions() {
        for (SessionGroup g : mList)
            if (g.isRunning())
                return true;
        return false;
    }

    public long getDuration() {
        long res = 0;
        for (SessionGroup g : mList)
            res += g.getDuration();
        return res;
    }

    public class SessionGroup {
        private long mID;
        private long mStartTime;
        private long mEndTime;
        private long mDuration;
        private int mUncompletedCount;

        SessionGroup(long id, long start, long end, long duration, int uncomplCount) {
            super();
            mID = id;
            mStartTime = start;
            mEndTime = end;
            mDuration = duration;
            mUncompletedCount = uncomplCount;
        }

        public boolean isRunning() {
            return mUncompletedCount > 0;
        }

        public long getStart() {
            return mStartTime;
        }

        public long getEnd() {
            return mEndTime;
        }

        public long getDuration() {
            long res = mDuration;
            if (mUncompletedCount > 0)
                res += mUncompletedCount * System.currentTimeMillis() / 1000L - mEndTime;
            return res;
        }

        public boolean sameData(long start, long end, long duration, int cnt) {
            return (mStartTime == start) &&
                    (mEndTime == end) &&
                    (mDuration == duration) &&
                    (mUncompletedCount == cnt);
        }

        public void updateData(long start, long end, long duration, int cnt) {
            mStartTime = start;
            mEndTime = end;
            mDuration = duration;
            mUncompletedCount = cnt;
        }
    }

    public enum GroupType {
        gt_None,
        gt_Day,
        gt_Week,
        gt_Month,
        gt_Year
    }

    public interface IGroupsChangesListener {
        void onDataChanged();
        void onItemRemoved(int index);
        void onItemChanged(int index);
        void onItemMoved(int oldIndex, int newIndex);
    }
}
