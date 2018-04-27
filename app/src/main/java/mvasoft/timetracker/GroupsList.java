package mvasoft.timetracker;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;

import mvasoft.timetracker.data.DatabaseDescription;
import mvasoft.timetracker.data.DatabaseDescription.GroupsDescription;
import mvasoft.utils.Announcer;

public class GroupsList {

    private final ArrayList<SessionGroup> mList;
    private final Announcer<IGroupsChangesListener> mAnnouncer;
    private Cursor mCursor;

    public GroupsList() {
        mList = new ArrayList<>();
        mAnnouncer = new Announcer<>(IGroupsChangesListener.class);
    }

    public long getDuration() {
        long res = 0;
        for (SessionGroup g : mList)
            res += g.getDuration();
        return res;
    }

    public SessionGroup getByID(long id) {
        for (SessionGroup g : mList)
            if (g.mID == id)
                return g;
        return null;
    }

    public boolean hasOpenedSessions() {
        for (SessionGroup g : mList)
            if (g.isRunning())
                return true;
        return false;
    }

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

    @SuppressWarnings("ConstantConditions")
    private void updateData() {
        if (mCursor == null) {
            notifyDataChanged();
            return;
        }

        HashSet<Long> processedIds = new HashSet<>();
        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            long id = mCursor.getLong(mCursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));

            int idx = indexByID(id);
            long start = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_START));
            long end = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_END));
            long duration = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_DURATION));
            int cnt = mCursor.getInt(mCursor.getColumnIndex(GroupsDescription.COLUMN_UNCOMPLETED_COUNT));

            if (idx < 0)
                mList.add(i, new SessionGroup(id, start, end, duration, cnt));
            else if (!get(idx).sameData(start, end, duration, cnt)) {
                get(idx).updateData(start, end, duration, cnt);
                if (idx != i)
                    mList.add(i, mList.remove(idx));
            }
            processedIds.add(id);
        }

        for (int i = count() - 1; i >= 0; i--)
            if (!processedIds.contains(get(i).getID()))
                mList.remove(i);
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

    private void notifyDataChanged() {
        mAnnouncer.announce().onDataChanged();
    }

    private int indexByID(long id) {
        for (int i = 0; i < count(); i++)
            if (get(i).mID == id)
                return i;
        return -1;
    }
    SessionGroup get(int idx) {
        if ((idx >= 0) && (idx < mList.size()))
            return mList.get(idx);
        else
            return null;
    }

    int count() {
        return mList.size();
    }

    public void addChangesListener(@NonNull IGroupsChangesListener listener) {
        mAnnouncer.addListener(listener);
    }

    public void removeChangesListener(@NonNull IGroupsChangesListener listener) {
        mAnnouncer.removeListener(listener);
    }

    public interface IGroupsChangesListener extends EventListener {
        void onDataChanged();
    }

    public class SessionGroup {
        private final long mID;
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

        boolean isRunning() {
            return mUncompletedCount > 0;
        }

        boolean sameData(long start, long end, long duration, int cnt) {
            return (mStartTime == start) &&
                    (mEndTime == end) &&
                    (mDuration == duration) &&
                    (mUncompletedCount == cnt);
        }

        void updateData(long start, long end, long duration, int cnt) {
            mStartTime = start;
            mEndTime = end;
            mDuration = duration;
            mUncompletedCount = cnt;
        }

        long getID() {
            return mID;
        }
    }

}
