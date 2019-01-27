package mvasoft.timetracker.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

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
        try {
            gson.toJson(view, writer);
            return true;
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean fromJson(AppDatabase db, Reader reader) {
        Gson gson = createGson();
        DatabaseView view = null;
        try {
            view = gson.fromJson(reader, DatabaseView.class);
        } catch (JsonIOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e){
            e.printStackTrace();
        }

        if (view == null)
            return false;

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
