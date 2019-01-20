package mvasoft.timetracker.core;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import io.reactivex.disposables.Disposable;
import mvasoft.timetracker.db.DatabaseProvider;
import timber.log.Timber;

import static android.content.Context.ACCOUNT_SERVICE;

@Singleton
public class SyncManager {

    private static final String ACCOUNT = "TimeTracker Sync";
    private static final String ACCOUNT_TYPE = "mvasoft.timetracker.sync";
    private static final String AUTHORITY = "mvasoft.timetracker.sync.provider";

    private final Account mAccount;
    private final Disposable mDisposable;

    @Inject
    SyncManager(@NonNull Context context, DatabaseProvider databaseProvider) {
        mAccount = createSyncAccount(context);
        mDisposable = databaseProvider.getDatabase()
                .subscribe(db -> db.getInvalidationTracker().addObserver(
                        new InvalidationTracker.Observer("sessions", "days") {
                            @Override
                            public void onInvalidated(@NonNull Set<String> tables) {
                                requestSync();
                            }
                        }));
    }

    public void clear() {
        Timber.d("Clear()");
        if (mDisposable != null)
            mDisposable.dispose();
    }

    private void requestSync() {
        Timber.d("Sync request");
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(mAccount, AUTHORITY, extras);
    }

    private static Account createSyncAccount(Context context) {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            Timber.d("Account created");
        } else {
            Timber.d("Account exists");
        }
        return newAccount;
    }

}
