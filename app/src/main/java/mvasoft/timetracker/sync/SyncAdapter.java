package mvasoft.timetracker.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import javax.inject.Inject;

import mvasoft.timetracker.db.DatabaseProvider;
import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final DatabaseProvider mDatabaseProvider;

    @Inject
    public SyncAdapter(Context context, DatabaseProvider databaseProvider) {
        super(context, true);
        mDatabaseProvider = databaseProvider;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Timber.d("Sync called");
    }
}
