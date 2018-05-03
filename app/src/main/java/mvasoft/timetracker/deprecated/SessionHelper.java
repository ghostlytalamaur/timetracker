package mvasoft.timetracker.deprecated;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import mvasoft.timetracker.deprecated.DatabaseDescription.SessionDescription;


public class SessionHelper {

    private static final long EMPTY_DURATION = -1;
    private static final long EMPTY_ID = -1;
    private final Context mContext;

    SessionHelper(@NonNull Context context) {
        super();
        mContext = context.getApplicationContext();
    }

    public ToggleSessionResult toggleSession() {
        return ToggleSessionResult.tgs_Error;
//        if (stopSession())
//            return ToggleSessionResult.tgs_Stopped;
//        else if (startNewSession())
//            return ToggleSessionResult.tgs_Started;
//
//        return ToggleSessionResult.tgs_Error;
    }

    boolean deleteGroups(Uri groupUri, long[] groupIds) {
        ArrayList<Long> ids = new ArrayList<>();
        for (long id : groupIds)
            ids.add(id);
        return deleteGroups(groupUri, ids);
    }

    private boolean deleteGroups(Uri groupUri, List<Long> groupIds) {
        if (groupIds == null)
            return false;

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (Long id : groupIds) {
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

    public boolean hasOpenedSessions() {
        return false;//getOpenedSessionID() != EMPTY_ID;
    }

    /**
     * Query total duration for today
     * @return long, count of seconds
     */
    long getTodayDuration() {
        return 0;
//
//        final String SQL_WHERE = String.format(
//                "date(%1$s, 'unixepoch') = date('now')",
//                SessionDescription.COLUMN_START);
//
//        Cursor cursor = mContext.getContentResolver().query(GroupsDescription.GROUP_DAY_URI,
//                new String[] {GroupsDescription.COLUMN_DURATION},
//                SQL_WHERE, null, DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
//        try {
//            if ((cursor == null) || (cursor.getCount() <= 0))
//                return EMPTY_DURATION;
//
//            cursor.moveToFirst();
//            int colIdx = cursor.getColumnIndex(GroupsDescription.COLUMN_DURATION);
//            if (colIdx < 0 || colIdx >= cursor.getColumnCount())
//                return EMPTY_DURATION;
//
//            return cursor.getLong(colIdx);
//        }
//        finally {
//            if (cursor != null)
//                cursor.close();
//        }
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

    public enum ToggleSessionResult {
        tgs_Started,
        tgs_Stopped,
        tgs_Error
    }
}
