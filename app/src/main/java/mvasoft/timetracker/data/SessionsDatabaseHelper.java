package mvasoft.timetracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import mvasoft.timetracker.data.DatabaseDescription.SessionDescription;

/**
 * Created by mihal on 13.10.2017.
 */

public class SessionsDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "timetracker.db";

    public SessionsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SESSIONS_TABLE =
                "CREATE TABLE " + SessionDescription.TABLE_NAME + " (" +
                SessionDescription._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SessionDescription.COLUMN_START + " INTEGER NOT NULL, " +
                SessionDescription.COLUMN_END + " INTEGER);";
        db.execSQL(CREATE_SESSIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
