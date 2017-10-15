package mvasoft.timetracker;

import android.database.Cursor;
import java.util.ArrayList;
import mvasoft.timetracker.data.DatabaseDescription.GroupsDescription;

class GroupsList {

    private ArrayList<SessionGroup> mList;
    private Cursor mCursor;

    GroupsList() {
        mList = new ArrayList<>();
    }


    // TODO: Refactoring for update changed groups and notification with modified/added/deleted/moved
    public void swapCursor(Cursor cursor) {
        if (mCursor != null)
            mCursor.close();
        mCursor = cursor;
        fillList();
    }

    private void fillList() {
        mList.clear();
        if (mCursor == null)
            return;

        for (int i = 0; i < mCursor.getCount(); i++) {
            mCursor.moveToPosition(i);
            // TODO: return minimal ID of session in group in content provider
//            long id = cursor.getLong(cursor.getColumnIndex(SessionDescription._ID));
            long start = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_START));
            long end = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_END));
            long duration = mCursor.getLong(mCursor.getColumnIndex(GroupsDescription.COLUMN_DURATION));
            int cnt = mCursor.getInt(mCursor.getColumnIndex(GroupsDescription.COLUMN_UNCOMPLETED_COUNT));
            mList.add(new SessionGroup(-1, start, end, duration, cnt));
        }
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

    }

    public enum GroupType {
        gt_None,
        gt_Day,
        gt_Week,
        gt_Month,
        gt_Year
    }
}
