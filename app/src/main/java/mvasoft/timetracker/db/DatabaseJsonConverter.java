package mvasoft.timetracker.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.Session;

public class DatabaseJsonConverter {

    private static Gson createGson() {
        return new GsonBuilder()
                .setVersion(1.0)
                .create();
    }

    public static boolean toJson(@NonNull AppDatabase db, Writer writer) {
        Gson gson = createGson();
        DatabaseView view = DatabaseView.from(db);
        gson.toJson(view, writer);
        return true;
    }

    public static boolean fromJson(AppDatabase db, Reader reader) {
        Gson gson = createGson();
        DatabaseView view = gson.fromJson(reader, DatabaseView.class);
        db.beginTransaction();
        try {
            db.groupsModel().clearSessions();
            db.groupsModel().appendAll(view.sessions);

            db.groupsModel().clearDays();
            db.groupsModel().appendDays(view.days);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private static class DatabaseView {
        List<Session> sessions;
        List<DayDescription> days;

        static DatabaseView from(@NonNull AppDatabase db) {
            DatabaseView view = new DatabaseView();
            db.beginTransaction();
            try {
                view.sessions = db.groupsModel().getSessionsRx().blockingFirst();
                view.days = db.groupsModel().getDayDescriptionsRx().blockingFirst();
                return view;
            } finally {
                db.endTransaction();
            }
        }
    }
}
