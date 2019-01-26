package mvasoft.timetracker.sync;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import mvasoft.timetracker.R;
import mvasoft.timetracker.core.InternalWorkerFactory;
import mvasoft.timetracker.db.DatabaseJsonConverter;
import mvasoft.timetracker.db.DatabaseProvider;
import timber.log.Timber;

public class DriveBackupWorker extends Worker {


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

        String dir = drive.createFolder("timetracker_backup");
        if (dir == null)
            return Result.failure();

        final String fileName = "timetracker_backup.json";
        String fileId = drive.createFile(fileName, dir);
        if (fileId != null) {

            StringWriter w = new StringWriter();
            DatabaseJsonConverter.toJson(mDatabaseProvider.getDatabase().getValue(), w);
            ByteArrayContent content = ByteArrayContent.fromString("application/json",
                    w.getBuffer().toString());

            if (drive.updateFile(fileId, fileName, content))
                return Result.success();
        }

        return Result.failure();
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
                AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
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
