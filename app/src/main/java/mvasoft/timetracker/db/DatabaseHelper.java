package mvasoft.timetracker.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.vo.Session;

public class DatabaseHelper {

    public static boolean importOldDb(@NonNull File file, @NonNull DataRepository repository) {
        SQLiteDatabase oldDb = null;
        Cursor cursor = null;
        try {
            oldDb = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            if (!oldDb.isOpen())
                return false;

            cursor = oldDb.rawQuery("SELECT * FROM sessions ORDER BY StartTime DESC", null);
            if (cursor == null || cursor.isClosed())
                return false;

            // CREATE TABLE "sessions"(_id INTEGER PRIMARY KEY AUTOINCREMENT, StartTime INTEGER NOT NULL, EndTime INTEGER )
            if (cursor.getCount() == 0 || cursor.getColumnCount() != 3 || !cursor.moveToFirst())
                return false;

            int startColIdx = cursor.getColumnIndex("StartTime");
            int endColIdx = cursor.getColumnIndex("EndTime");
            if (startColIdx < 0 || endColIdx < 0)
                return false;

            ArrayList<Session> list = new ArrayList<>();
            do {
                long start = cursor.getLong(startColIdx);
                long end = 0;
                if (!cursor.isNull(endColIdx))
                    end = cursor.getLong(endColIdx);

                list.add(new Session(0, start, end));

            } while (cursor.moveToNext());

            repository.appendAll(list);
            return true;
        }
        catch (Throwable e){
            e.printStackTrace();
            return false;
        }
        finally {
            if (cursor != null)
                cursor.close();
            if (oldDb != null)
                oldDb.close();
        }

    }
}
