package mvasoft.timetracker.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;

@Database(entities = {Session.class, DayDescription.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SessionsDao groupsModel();

    public static AppDatabase getDatabase(final Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "timetracker.db")
                .build();
    }

}
