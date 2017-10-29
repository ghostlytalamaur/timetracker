package mvasoft.timetracker;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import mvasoft.timetracker.data.DatabaseDescription;

class BackupAssistant {

    private static final String BACKUP_FOLDER_NAME = "TimeTrackerBackups";
    private final Context mContext;

    private BackupAssistant(Context context) {
        super();
        mContext = context;
    }

    static void backupDb(Context context) {
        BackupAssistant assist = new BackupAssistant(context);
        assist.BackupDB();
    }

    private boolean BackupDB() {
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

    public static void importDb(Context context) {
        BackupAssistant assist = new BackupAssistant(context);
        assist.RestoreDB();
    }

    private boolean RestoreDB() {
        backupRestore(false);
        mContext.getContentResolver().notifyChange(DatabaseDescription.GroupsDescription.GROUP_NONE_URI, null);
        return true;
//        SQLiteDatabase db = new SQLiteDatabase.
//
//        mContext.getContentResolver().delete(DatabaseDescription.SessionDescription.CONTENT_URI,
//                null, null);


//        return true;
    }
}
