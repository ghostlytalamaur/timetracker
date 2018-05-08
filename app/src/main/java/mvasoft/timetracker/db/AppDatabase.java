package mvasoft.timetracker.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;

@Database(entities = {Session.class, DayDescription.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract SessionsDao groupsModel();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "timetracker.db")
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    createTriggers(db);
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void createTriggers(SupportSQLiteDatabase db) {
    }

}