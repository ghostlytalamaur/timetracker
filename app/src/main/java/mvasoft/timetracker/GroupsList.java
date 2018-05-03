package mvasoft.timetracker;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Observable;

import mvasoft.timetracker.data.DatabaseDescription;
import mvasoft.timetracker.data.DatabaseDescription.GroupsDescription;
import mvasoft.utils.Announcer;

public class GroupsList {

    private final ArrayList<SessionGroup> mList;
    private final Announcer<IGroupsChangesListener> mAnnouncer;

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

    @SuppressWarnings("ConstantConditions")
    public void updateData(Cursor cursor) {
        if ((cursor == null) || (cursor.isClosed())) {
            mList.clear();
            notifyDataChanged();
            return;
        }

        boolean wasChanged = count() != cursor.getCount();
        HashSet<Long> processedIds = new HashSet<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            long id = cursor.getLong(cursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));

            int idx = indexByID(id);
            long start = cursor.getLong(cursor.getColumnIndex(GroupsDescription.COLUMN_START));
            long end = cursor.getLong(cursor.getColumnIndex(GroupsDescription.COLUMN_END));
            long duration = cursor.getLong(cursor.getColumnIndex(GroupsDescription.COLUMN_DURATION));
            int cnt = cursor.getInt(cursor.getColumnIndex(GroupsDescription.COLUMN_UNCOMPLETED_COUNT));

            if (idx < 0) {
                mList.add(i, new SessionGroup(id, start, end, duration, cnt));
                wasChanged = true;
            }
            else if (!get(idx).sameData(start, end, duration, cnt)) {
                get(idx).updateData(start, end, duration, cnt);
                wasChanged = true;
                if (idx != i)
                    mList.add(i, mList.remove(idx));
            }
            processedIds.add(id);
        }

        for (int i = count() - 1; i >= 0; i--)
            if (!processedIds.contains(get(i).getID()))
                mList.remove(i);

        if (wasChanged)
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
    public SessionGroup get(int idx) {
        if ((idx >= 0) && (idx < mList.size()))
            return mList.get(idx);
        else
            return null;
    }

    public void add(SessionGroup group) {
        if (group != null)
            mList.add(group);
    }

    public int count() {
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

    public static class SessionGroup extends Observable {
        private static final long TARGET_SEC = 8 * 60 * 60;
        private final long mID;
        private long mStartTime;
        private long mEndTime;
        private long mDuration;
        private int mUncompletedCount;

        public SessionGroup(long id, long start, long end, long duration, int uncomplCount) {
            super();
            mID = id;
            mStartTime = start;
            mEndTime = end == 0 ? System.currentTimeMillis() / 1000 : end;
            mDuration = duration;
            mUncompletedCount = end == 0 ? 1 : 0; //uncomplCount;
            mDuration = end == 0 ? 0 : mEndTime - mStartTime;
        }

        public long getStart() {
            return mStartTime;
        }

        public long getEnd() {
            return mEndTime;
        }

        public long getDuration() {
            // FIXME: 03.05.2018
            long res = mDuration;
            if (mUncompletedCount > 0)
                res += mUncompletedCount * System.currentTimeMillis() / 1000L - mEndTime;
            return res;
        }

        public boolean isRunning() {
            return (mUncompletedCount > 0) || (mEndTime == 0);
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
            setChanged();
            notifyObservers();
        }

        public long getID() {
            return mID;
        }

        public long getGoalTimeDiff() {
            return getDuration() - TARGET_SEC;
        }

        public boolean isGoalAchieved() {
            return getGoalTimeDiff() >= 0;
        }

        public void dataChanged() {
            setChanged();
            notifyObservers();
        }
    }

}
