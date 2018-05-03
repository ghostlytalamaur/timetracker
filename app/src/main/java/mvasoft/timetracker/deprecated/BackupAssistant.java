package mvasoft.timetracker.deprecated;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupAssistant {

    private static final String BACKUP_FOLDER_NAME = "TimeTrackerBackups";

    private final Context mContext;

    private BackupAssistant(Context context) {
        super();
        mContext = context;
    }

    static boolean backupDb(Context context) {
        BackupAssistant assist = new BackupAssistant(context);
        return assist.doBackup();
    }

    static boolean restoreDb(Context context) {
        BackupAssistant assist = new BackupAssistant(context);
        return assist.doRestore();
    }

    private boolean doBackup() {
        return backupRestore(true);
    }

    private boolean backupRestore(boolean isBackup) {
        String backupPath = getBackupFolderPath();
        if (backupPath == null)
            return false;

        if (isBackup)
            return doTransferFile(getDBPath(), backupPath + "/" + DatabaseDescription.DATABASE_NAME);
        else
            return doTransferFile(backupPath + "/" + DatabaseDescription.DATABASE_NAME, getDBPath());
    }

    private String getBackupFolderPath() {
        File sd = Environment.getExternalStorageDirectory();
        if (!sd.canWrite() || !sd.canRead())
            return null;

        File destFile = new File(sd.getPath() + "/" + BACKUP_FOLDER_NAME);
        destFile.mkdirs();
        return destFile.getPath();
    }

    private boolean doTransferFile(String aSourcePath, String aDestPath) {
        try{
            File sourceFile = new File(aSourcePath);
            File destFile = new File(aDestPath);

            FileChannel src = new FileInputStream(sourceFile).getChannel();
            FileChannel dst = new FileOutputStream(destFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getDBPath() {
        return mContext.getDatabasePath(DatabaseDescription.DATABASE_NAME).getPath();
    }

    private boolean doRestore() {
        boolean res = backupRestore(false);
        ContentResolver resolver = mContext.getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(DatabaseDescription.AUTHORITY);
        try {
            if (client == null)
                return res;

            SessionsContentProvider provider = (SessionsContentProvider) client.getLocalContentProvider();
            if (provider != null)
                provider.resetDatabase();
        } finally {
            if (client != null)
                client.release();
        }
        return res;
    }
}
