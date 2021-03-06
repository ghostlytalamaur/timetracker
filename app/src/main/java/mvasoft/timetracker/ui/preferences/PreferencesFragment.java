package mvasoft.timetracker.ui.preferences;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import mvasoft.dialogs.AlertDialogFragment;
import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.timetracker.R;
import mvasoft.timetracker.sync.DriveBackupWorker;

public class PreferencesFragment extends PreferenceFragmentCompat
        implements DialogResultListener {

    private static final int PERMISSION_REQUEST_STORAGE_BACKUP = 2;
    private static final int PERMISSION_REQUEST_STORAGE_RESTORE = 3;
    private static final int DLG_REQUEST_RESTORE_DB = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        findPreference(getString(R.string.prefs_key_perform_backup)).setOnPreferenceClickListener(
                preference -> {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_STORAGE_BACKUP);
                    return true;
                }
        );

        findPreference(getString(R.string.prefs_key_perform_restore)).setOnPreferenceClickListener(
                (preference) -> {
                    new AlertDialogFragment.Builder(DLG_REQUEST_RESTORE_DB)
                            .withMessage(getString(R.string.msg_all_session_will_removed))
                            .show(this, "PrefsFragment" + DLG_REQUEST_RESTORE_DB);
                    return true;
                }
        );
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

    @Override
    public void onDialogResult(@NonNull DialogResultData dialogResultData) {
        switch (dialogResultData.requestCode) {
            case DLG_REQUEST_RESTORE_DB:
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE_RESTORE);
                break;
        }
    }

    private void showToast(@StringRes int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_LONG).show();
    }

    private void doBackupRestoreDb(boolean isBackup) {
        if (getContext() == null)
            return;

        if (GoogleSignIn.getLastSignedInAccount(getContext()) == null) {
            requestSignIn();
            return;
        }

        Constraints driveConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest driveRequest = new OneTimeWorkRequest.Builder(DriveBackupWorker.class)
                .setConstraints(driveConstraints)
                .setInputData(DriveBackupWorker.makeArgs(isBackup))
                .build();
        WorkManager.getInstance()
                .beginWith(driveRequest)
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

        WorkManager.getInstance().getWorkInfoByIdLiveData(driveRequest.getId())
                .observe(this, workInfoObserver);
    }

    private void requestSignIn() {
        if (getContext() == null)
            return;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder()
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(getContext(), gso);
        startActivity(client.getSignInIntent());
    }

}
