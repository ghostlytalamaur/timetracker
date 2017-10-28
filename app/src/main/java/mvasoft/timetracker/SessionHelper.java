package mvasoft.timetracker;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import mvasoft.timetracker.data.DatabaseDescription;
import mvasoft.timetracker.data.DatabaseDescription.GroupsDescription;
import mvasoft.timetracker.data.DatabaseDescription.SessionDescription;


class SessionHelper {

    private static final long EMPTY_ID = -1;
    static final long EMPTY_DURATION = -1;
    private Context mContext;

    SessionHelper(@NonNull Context context) {
        super();
        mContext = context;
    }

    ToggleSessionResult toggleSession() {
        if (stopSession())
            return ToggleSessionResult.tgs_Started;
        else if (startNewSession())
            return ToggleSessionResult.tgs_Stopped;

        return ToggleSessionResult.tgs_Error;
    }

    boolean deleteGroups(Uri groupUri, long[] groupIds) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (long id : groupIds) {
            ContentProviderOperation op = ContentProviderOperation.newDelete(
                    Uri.withAppendedPath(groupUri, Long.toString(id))).build();
            operations.add(op);
        }

        try {
            mContext.getContentResolver().applyBatch(DatabaseDescription.AUTHORITY, operations);
            return true;
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }

        return false;
    }

    boolean hasOpenedSessions() {
        return getOpenedSessionID() != EMPTY_ID;
    }


    /**
     * Query total duration for today
     * @return long, count of seconds
     */
    long getTodayDuration() {
        final String SQL_WHERE = String.format(
                "date(%1$s, 'unixepoch') = date('now')",
                SessionDescription.COLUMN_START);

        Cursor cursor = mContext.getContentResolver().query(GroupsDescription.GROUP_DAY_URI,
                new String[] {GroupsDescription.COLUMN_DURATION},
                SQL_WHERE, null, DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
        try {
            if ((cursor == null) || (cursor.getCount() <= 0))
                return EMPTY_DURATION;

            cursor.moveToFirst();
            int colIdx = cursor.getColumnIndex(GroupsDescription.COLUMN_DURATION);
            if (colIdx < 0 || colIdx >= cursor.getColumnCount())
                return EMPTY_DURATION;

            return cursor.getLong(colIdx);
        }
        finally {
            if (cursor != null)
                cursor.close();
        }
    }

    long getCurrentDuration() {
        final String SQL_WHERE = String.format(
                "((date(%1$s, 'unixepoch') = date('now')) AND (%2$s is NULL))",
                SessionDescription.COLUMN_START,
                SessionDescription.COLUMN_END);

        final String SQL_PROJ = String.format(
                "CASE WHEN %2$s is NULL THEN strftime('%%s', 'now') - %1$s ELSE %2$s - %1$s END as Duration",
                SessionDescription.COLUMN_START,
                SessionDescription.COLUMN_END
        );

        Cursor cursor = mContext.getContentResolver().query(SessionDescription.CONTENT_URI,
                new String[] {SQL_PROJ},
                SQL_WHERE, null, DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
        try {
            if ((cursor == null) || (cursor.getCount() <= 0))
                return EMPTY_DURATION;

            cursor.moveToFirst();
            int colIdx = cursor.getColumnIndex("Duration");
            if (colIdx < 0 || colIdx >= cursor.getColumnCount())
                return EMPTY_DURATION;

            return cursor.getLong(colIdx);
        }
        finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private boolean stopSession() {

        long id = getOpenedSessionID();
        if (id == EMPTY_ID)
            return false;

        ContentValues values = new ContentValues();
        values.put(SessionDescription.COLUMN_END, System.currentTimeMillis() / 1000L);

        return mContext.getContentResolver().update(SessionDescription.buildSessionUri(id),
                values, null, null) > 0;
    }

    private boolean startNewSession() {
        ContentValues values = new ContentValues();
        values.put(SessionDescription.COLUMN_START,
                System.currentTimeMillis() / 1000L);
        return mContext.getContentResolver().insert(SessionDescription.CONTENT_URI, values) != null;
    }

    boolean updateSession(long id, long start, long end) {
        ContentValues values = new ContentValues();
        values.put(SessionDescription.COLUMN_START, start);
        values.put(SessionDescription.COLUMN_END, end);
        return mContext.getContentResolver().update(SessionDescription.buildSessionUri(id),
                values, null, null) > 0;
    }

    private long getOpenedSessionID() {
        Cursor cursor = mContext.getContentResolver().query(
                SessionDescription.CONTENT_URI, null,
                SessionDescription.COLUMN_END + " is null", null, null);
        if ((cursor == null) || (cursor.getCount() <= 0))
            return EMPTY_ID;

        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(SessionDescription._ID));
        cursor.close();
        return id;
    }

    enum ToggleSessionResult {
        tgs_Started,
        tgs_Stopped,
        tgs_Error
    }
}
