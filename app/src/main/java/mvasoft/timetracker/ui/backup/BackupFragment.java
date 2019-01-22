package mvasoft.timetracker.ui.backup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.Observer;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import mvasoft.dialogs.AlertDialogFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.timetracker.R;
import mvasoft.timetracker.databinding.FragmentBackupBinding;
import mvasoft.timetracker.sync.LocalBackupWorker;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.common.BindingSupportFragment;

public class BackupFragment extends BindingSupportFragment<FragmentBackupBinding, BaseViewModel>
        implements DialogResultListener {

    private static final int PERMISSION_REQUEST_STORAGE_BACKUP = 2;
    private static final int PERMISSION_REQUEST_STORAGE_RESTORE = 3;
    private static final int DLG_REQUEST_RESTORE_DB = 1;

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
                doBackupRestoreDb(true);
                break;
            case PERMISSION_REQUEST_STORAGE_RESTORE:
                doBackupRestoreDb(false);
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

    private void showToast(@StringRes int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_LONG).show();
    }

    private void doBackupRestoreDb(boolean isBackup) {
        if (!isBackup && !isExternalStorageReadable() ||
                isBackup && !isExternalStorageWritable()) {
            showToast(R.string.msg_external_storage_unavailable);
            return;
        }

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LocalBackupWorker.class)
                .setInputData(LocalBackupWorker.makeArgs(isBackup))
                .build();
        WorkManager.getInstance()
                .beginWith(request)
                .enqueue();

        Observer<? super WorkInfo> workInfoObserver = workInfo -> {
            switch (workInfo.getState()) {
                case FAILED: {
                    showToast(isBackup ? R.string.msg_cannot_backup_db : R.string.msg_cannot_restore_db);
                    break;
                }
                case SUCCEEDED: {
                    showToast(isBackup ? R.string.msg_backup_db_succeeded : R.string.msg_restore_db_succeeded);
                    break;
                }
                case CANCELLED: {
                    showToast(isBackup ? R.string.msg_backup_db_cancelled : R.string.msg_restore_db_cancelled);
                    break;
                }
            }
        };

        WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId())
                .observe(this, workInfoObserver);
    }

}
