package mvasoft.timetracker.data.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import mvasoft.timetracker.data.room.entity.SessionEntity;

@Database(entities = {SessionEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract GroupsListDao groupsModel();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "timetracker.db")
//                            .allowMainThreadQueries()
                            .addMigrations()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
