package mvasoft.timetracker.sync;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.DatabaseJsonConverter;
import timber.log.Timber;

class BackupHelper {

    private final AppDatabase mDatabase;

    BackupHelper(AppDatabase database) {
        mDatabase = database;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private File getBackupFilePath() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                "TimeTrackerBackups");
        if (!dir.exists() && !dir.mkdirs()) {
            Timber.d("Cannot create dirs: %s", dir.toString());
            return null;
        }
        return new File(dir, "timetracker_backup.json");
    }

    boolean backup() throws IOException {
        if (!isExternalStorageWritable())
            return false;

        File file = getBackupFilePath();
        if (file == null)
            return false;

        FileOutputStream outStream = new FileOutputStream(file, false);
        try (Writer writer = new OutputStreamWriter(outStream)) {
            return DatabaseJsonConverter.toJson(mDatabase, writer);
        }
    }

    boolean restore() throws IOException {
        if (!isExternalStorageReadable())
            return false;

        File file = getBackupFilePath();
        if (file == null)
            return false;

        FileInputStream inputStream = new FileInputStream(file);
        try (Reader reader = new InputStreamReader(inputStream)) {
            return DatabaseJsonConverter.fromJson(mDatabase, reader);
        }
    }

}
