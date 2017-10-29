package mvasoft.timetracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import mvasoft.timetracker.data.DatabaseDescription.SessionDescription;

import static mvasoft.timetracker.data.DatabaseDescription.DATABASE_NAME;

class SessionsDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    SessionsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SESSIONS_TABLE =
                "CREATE TABLE IF NOT EXISTS " + SessionDescription.TABLE_NAME + " (" +
                SessionDescription._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SessionDescription.COLUMN_START + " INTEGER NOT NULL, " +
                SessionDescription.COLUMN_END + " INTEGER);";
        db.execSQL(CREATE_SESSIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
