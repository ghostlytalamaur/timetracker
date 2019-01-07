package mvasoft.timetracker.ui.backup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import javax.inject.Inject;

import mvasoft.dialogs.AlertDialogFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.timetracker.R;
import mvasoft.timetracker.core.Injectable;
import mvasoft.timetracker.databinding.FragmentBackupBinding;
import mvasoft.timetracker.db.DatabaseProvider;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.utils.FileUtils;

public class BackupFragment extends BindingSupportFragment<FragmentBackupBinding, BaseViewModel>
        implements DialogResultListener, Injectable {

    private static final int PERMISSION_REQUEST_STORAGE_BACKUP = 2;
    private static final int PERMISSION_REQUEST_STORAGE_RESTORE = 3;
    private static final int DLG_REQUEST_RESTORE_DB = 1;

    @Inject
    DatabaseProvider mDatabaseProvider;

    @Override
    public void onDialogResult(@NonNull DialogResultData dialogResultData) {
        switch (dialogResultData.requestCode) {
            case DLG_REQUEST_RESTORE_DB:
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE_RESTORE);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =  super.onCreateView(inflater, container, savedInstanceState);
        getBinding().btnBackup.setOnClickListener(v -> checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE_BACKUP));
        getBinding().btnRestore.setOnClickListener(v ->
                new AlertDialogFragment.Builder(DLG_REQUEST_RESTORE_DB)
                        .withMessage(getString(R.string.msg_all_session_will_removed))
                        .show(this, "BackupFragment" + DLG_REQUEST_RESTORE_DB));
        return rootView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_backup;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(),
                    "Cannot perform operation without appropriate permission",
                    Toast.LENGTH_LONG).show();
            return;
        }

        switch (requestCode) {
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

    private void checkPermission(@NonNull String permission, int requestCode) {
        if (getActivity() != null)
            requestPermissions(new String[]{permission}, requestCode);
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

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private boolean isValidSQLiteDB(File backupDb) {
        return backupDb != null && backupDb.exists();
    }


    private void restoreDb() {
        if (getActivity() == null)
            return;

        if (!isExternalStorageReadable()) {
            showToast("External storage unavailable");
            return;
        }

        File dbFile = getActivity().getDatabasePath(mDatabaseProvider.getDatabase().getOpenHelper().getDatabaseName());
        File backup = getBackupFilePath();
        if (!backup.exists()) {
            showToast("There are no any backups");
            return;
        }
        if (!isValidSQLiteDB(backup)) {
            showToast("Backup database corrupted");
            return;
        }

        mDatabaseProvider.getDatabase().close();
        try {
            if (!FileUtils.copyFile(backup, dbFile)) {
                showToast("Cannot restore database");
                return;
            }


            showToast("Database restored");
        } finally{
            mDatabaseProvider.reinitDatabase(getActivity().getApplication());
        }
    }

    private File getBackupFilePath() {
        return new File(
                new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "TimeTrackerBackups"),
                "timetracker_backup.db");
    }

    private void backupDb() {
        if (getActivity() == null)
            return;

        if (!isExternalStorageWritable()) {
            showToast("External storage unavailable");
            return;
        }

        mDatabaseProvider.getDatabase().close();
        try {
            File dbFile = getActivity().getDatabasePath(mDatabaseProvider.getDatabase().getOpenHelper().getDatabaseName());

            File backupPath = getBackupFilePath();
            if (FileUtils.copyFile(dbFile, backupPath))
                showToast("Database saved in " + backupPath);
            else
                showToast("Cannot backup database");
        } finally {
            mDatabaseProvider.reinitDatabase(getActivity().getApplication());
        }
    }

}
