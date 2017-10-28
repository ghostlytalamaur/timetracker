package mvasoft.timetracker;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;

import mvasoft.timetracker.data.DatabaseDescription;
import mvasoft.timetracker.data.DatabaseDescription.GroupsDescription;
import mvasoft.utils.Announcer;

class GroupsList {

    private IGroupsChangesListener mListener;
    private ArrayList<SessionGroup> mList;
    private Cursor mCursor;
    private Announcer<IGroupsChangesListener> mAnnouncer;

    GroupsList() {
        mList = new ArrayList<>();
        mAnnouncer = new Announcer<>(IGroupsChangesListener.class);
    }


    void swapCursor(Cursor cursor) {
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
        mAnnouncer.announce().onDataChanged();
    }

    private void notifyItemRemoved(int index) {
        mAnnouncer.announce().onItemRemoved(index);
    }

    private void notifyItemChanged(int idx) {
        mAnnouncer.announce().onItemChanged(idx);
    }

    private void notifyItemMoved(int idx, int i) {
        mAnnouncer.announce().onItemMoved(idx, i);
    }

    private void notifyItemInserted(int index) {
        mAnnouncer.announce().onItemInserted(index);
    }

    void setChangesListener(IGroupsChangesListener listener) {
        addChangesListener(listener);
    }

    void addChangesListener(@NonNull IGroupsChangesListener listener) {
        mAnnouncer.addListener(listener);
    }

    void removeChangesListener(@NonNull IGroupsChangesListener listener) {
        mAnnouncer.removeListener(listener);
    }

    private void updateData() {
        if (mCursor == null) {
            notifyDataChanged();
            return;
        }
        boolean useOneNotification = true;

        HashSet<Long> processedIds = new HashSet<>();
        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            long id = mCursor.getLong(mCursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));

            int idx = indexByID(id);
            long start = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_START));
            long end = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_END));
            long duration = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_DURATION));
            int cnt = mCursor.getInt(mCursor.getColumnIndex(GroupsDescription.COLUMN_UNCOMPLETED_COUNT));

            if (idx < 0) {
                mList.add(i, new SessionGroup(id, start, end, duration, cnt));
                if (!useOneNotification)
                    notifyItemInserted(i);
            }
            else if (!get(idx).sameData(start, end, duration, cnt)) {
                get(idx).updateData(start, end, duration, cnt);
                if (!useOneNotification)
                    notifyItemChanged(idx);
                if (idx != i) {
                    mList.add(i, mList.remove(idx));
                    if (!useOneNotification)
                        notifyItemMoved(idx, i);
                }
            }
            processedIds.add(id);
        }

        for (int i = count() - 1; i >= 0; i--)
            if (!processedIds.contains(get(i).getID())) {
                mList.remove(i);
                if (!useOneNotification)
                    notifyItemRemoved(i);
            }
        if (useOneNotification)
            notifyDataChanged();
    }


    private void fillList() {
        mList.clear();
        if (mCursor == null) {
            notifyDataChanged();
            return;
        }

        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            long id = mCursor.getLong(mCursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));
            long start = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_START));
            long end = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_END));
            long duration = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_DURATION));
            int cnt = mCursor.getInt(mCursor.getColumnIndex(GroupsDescription.COLUMN_UNCOMPLETED_COUNT));
            mList.add(new SessionGroup(id, start, end, duration, cnt));
        }
        notifyDataChanged();
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

        public long getID() {
            return mID;
        }
    }

    public interface IGroupsChangesListener extends EventListener {
        void onDataChanged();
        void onItemRemoved(int index);
        void onItemChanged(int index);
        void onItemMoved(int oldIndex, int newIndex);
        void onItemInserted(int index);
    }

}
