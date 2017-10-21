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


class SessionHelper {

    private static final long EMPTY_ID = -1;
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

    private boolean stopSession() {

        long id = getOpenedSessionID();
        if (id == EMPTY_ID)
            return false;

        ContentValues values = new ContentValues();
        values.put(DatabaseDescription.SessionDescription.COLUMN_END, System.currentTimeMillis() / 1000L);

        return mContext.getContentResolver().update(DatabaseDescription.SessionDescription.buildSessionUri(id),
                values, null, null) > 0;
    }

    private boolean startNewSession() {
        ContentValues values = new ContentValues();
        values.put(DatabaseDescription.SessionDescription.COLUMN_START,
                System.currentTimeMillis() / 1000L);
        return mContext.getContentResolver().insert(DatabaseDescription.SessionDescription.CONTENT_URI, values) != null;
    }

    private long getOpenedSessionID() {
        Cursor cursor = mContext.getContentResolver().query(
                DatabaseDescription.SessionDescription.CONTENT_URI, null,
                DatabaseDescription.SessionDescription.COLUMN_END + " is null", null, null);
        if ((cursor == null) || (cursor.getCount() <= 0))
            return EMPTY_ID;

        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(DatabaseDescription.SessionDescription._ID));
        cursor.close();
        return id;
    }

    enum ToggleSessionResult {
        tgs_Started,
        tgs_Stopped,
        tgs_Error
    }
}
