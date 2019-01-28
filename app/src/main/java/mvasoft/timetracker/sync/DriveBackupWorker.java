package mvasoft.timetracker.sync;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import mvasoft.timetracker.R;
import mvasoft.timetracker.core.InternalWorkerFactory;
import mvasoft.timetracker.db.DatabaseJsonConverter;
import mvasoft.timetracker.db.DatabaseProvider;
import timber.log.Timber;

public class DriveBackupWorker extends Worker {

    private static final String BACKUP_DIR = "timetracker_backup";
    private static final String BACKUP_DB_FILE = "timetracker_backup.json";
    private static final String ARGS_IS_BACKUP = "args_is_backup";
    private static final String BACKUP_FILE = "timetracker_bakcup.zip";

    private final DatabaseProvider mDatabaseProvider;

    private DriveBackupWorker(@NonNull Context context,
                              @NonNull WorkerParameters workerParams,
                              DatabaseProvider databaseProvider) {
        super(context, workerParams);
        mDatabaseProvider = databaseProvider;
    }

    @NonNull
    @Override
    public Result doWork() {
        DriveHelper drive = signIn();
        if (drive == null) {
            return Result.failure();
        }

        boolean isBackup = getInputData().getBoolean(ARGS_IS_BACKUP, true);
        if (isBackup && backup(drive) || !isBackup && restore(drive)) {
            return Result.success();
        } else {
            return Result.failure();
        }
    }

    private boolean backup(DriveHelper drive) {
        String dir = drive.createFolder(BACKUP_DIR);
        if (dir == null)
            return false;

        String fileId = drive.createFile(BACKUP_FILE, dir);
        if (fileId != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZipOutputStream zOut = new ZipOutputStream(out);
            OutputStreamWriter writer = new OutputStreamWriter(zOut);
            try {
                zOut.putNextEntry(new ZipEntry(BACKUP_DB_FILE));

                DatabaseJsonConverter.toJson(mDatabaseProvider.getDatabase().getValue(), writer);
                writer.flush();
                zOut.closeEntry();
                zOut.finish();

                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

                return drive.updateFile(fileId, BACKUP_FILE, "application/zip", in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean restore(DriveHelper drive) {
        try (InputStream is = drive.getFileContent(BACKUP_DIR, BACKUP_FILE);
             Reader reader = is != null ? new InputStreamReader(new BufferedInputStream(is)) : null) {
            if (reader != null) {
                ZipInputStream zIn = new ZipInputStream(is);
                zIn.getNextEntry();
                Reader zReader = new InputStreamReader(zIn);

                boolean wasOk = DatabaseJsonConverter.fromJson(
                        mDatabaseProvider.getDatabase().getValue(),
                        zReader);
                zIn.closeEntry();
                return wasOk;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Data makeArgs(boolean isBackup) {
        return new Data.Builder()
                .putBoolean(ARGS_IS_BACKUP, isBackup)
                .build();
    }

    private DriveHelper signIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder()
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(
                getApplicationContext(), gso);

        Task<GoogleSignInAccount> accountTask = client.silentSignIn();
        GoogleSignInAccount account = null;
        try {
            account = Tasks.await(accountTask);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (account == null) {
            Timber.d("Cannot signIn.");
            return null;
        }

        Timber.d("Logged in with: %s", account.getEmail());
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Collections.singletonList(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        Drive drive = new Drive.Builder(
                new NetHttpTransport(), new GsonFactory(), credential)
                .setApplicationName(getApplicationContext().getString(R.string.app_name))
                .build();
        return new DriveHelper(drive);
    }

    public static class Factory extends InternalWorkerFactory<DriveBackupWorker> {

        private final DatabaseProvider mDatabaseProvider;

        @Inject
        public Factory(DatabaseProvider databaseProvider) {
            mDatabaseProvider = databaseProvider;
        }

        @Override
        public DriveBackupWorker create(Context context, WorkerParameters parameters) {
            return new DriveBackupWorker(context, parameters, mDatabaseProvider);
        }
    }
}
