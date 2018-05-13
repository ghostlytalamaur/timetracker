package mvasoft.timetracker.ui.backup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.R;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.data.room.RoomDataRepositoryImpl;
import mvasoft.timetracker.databinding.ActivityBackupBinding;
import mvasoft.timetracker.db.AppDatabase;
import mvasoft.timetracker.db.DatabaseHelper;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.common.BindingSupportActivity;
import mvasoft.utils.FileUtils;

public class BackupActivity extends BindingSupportActivity<ActivityBackupBinding, BaseViewModel> {

    private static final int PERMISSION_REQUEST_STORAGE_OLD_IMPORT = 1;
    private static final int PERMISSION_REQUEST_STORAGE_BACKUP = 2;
    private static final int PERMISSION_REQUEST_STORAGE_RESTORE = 3;
    private static final int ACTIVITY_REQUEST_OLD_IMPORT = 1;

    @Inject
    Lazy<DataRepository> mRepository;
    private AppDatabase mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = ((RoomDataRepositoryImpl) mRepository.get()).getDatabase();

        getBinding().btnBackup.setOnClickListener(v -> checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE_BACKUP));
        getBinding().btnRestore.setOnClickListener(v -> checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE_RESTORE));
        getBinding().btnImportOld.setOnClickListener(v -> checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE_OLD_IMPORT));
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_backup;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Cannot perform operation without appropriate permission",
                    Toast.LENGTH_LONG).show();
            return;
        }

        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE_OLD_IMPORT:
                selectDbToImport(ACTIVITY_REQUEST_OLD_IMPORT);
                break;
            case PERMISSION_REQUEST_STORAGE_BACKUP:
                backupDb();
                break;
            case PERMISSION_REQUEST_STORAGE_RESTORE:
                restoreDb();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_REQUEST_OLD_IMPORT:
                if (resultCode == RESULT_OK && data != null && data.getData() != null)
                    importOldDb(data.getData());
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void selectDbToImport(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent = Intent.createChooser(intent, "Select db");
        startActivityForResult(intent, requestCode);
    }


    private void checkPermission(@NonNull String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private void restart() {

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void importOldDb(Uri uri) {
        File file = null;
        try {
            file = FileUtils.createTempFileInputStream(getContentResolver().openInputStream(uri));
            DatabaseHelper.importOldDb(file, mRepository.get());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (file != null)
                //noinspection ResultOfMethodCallIgnored
                file.delete();
        }
    }

    private boolean isValidSQLiteDB(File backupDb) {
        return backupDb != null && backupDb.exists();
    }


    private void restoreDb() {
        if (!isExternalStorageReadable()) {
            showToast("External storage unavailable");
            return;
        }

        File dbFile = getDatabasePath(mDatabase.getOpenHelper().getDatabaseName());
        File backup = getBackupFilePath();
        if (!backup.exists()) {
            showToast("There are no any backups");
            return;
        }
        if (!isValidSQLiteDB(backup)) {
            showToast("Backup database corrupted");
            return;
        }

        mDatabase.close();
        if (!FileUtils.copyFile(backup, dbFile)) {
            showToast("Cannot restore database");
            return;
        }


        showToast("Database restored");
        if (mRepository.get() instanceof RoomDataRepositoryImpl) {
            ((RoomDataRepositoryImpl) mRepository.get()).reinitDatabase(getApplication());
            mDatabase = ((RoomDataRepositoryImpl) mRepository.get()).getDatabase();
        }
    }

    private File getBackupFilePath() {
        return new File(
                new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "TimeTrackerBackups"),
                "timetracker_backup.db");
    }

    private void backupDb() {
        if (!isExternalStorageWritable()) {
            showToast("External storage unavailable");
            return;
        }

        File dbFile = getDatabasePath(mDatabase.getOpenHelper().getDatabaseName());

        File backupPath = getBackupFilePath();
        if (FileUtils.copyFile(dbFile, backupPath))
            showToast("Database saved in " + backupPath);
        else
            showToast("Cannot backup database");
    }

}
