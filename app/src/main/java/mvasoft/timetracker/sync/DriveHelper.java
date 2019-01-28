package mvasoft.timetracker.sync;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import timber.log.Timber;

class DriveHelper {

    private final Drive mDrive;


    DriveHelper(Drive drive) {
        mDrive = drive;
    }

    String createFolder(String name) {
        File dir = searchFolder(name);
        if (dir != null) {
            Timber.d("Directory %s already exists", name);
            return dir.getId();
        }

        File metadata = new File()
                .setParents(Collections.singletonList("root"))
                .setName(name)
                .setAppProperties(getProps())
                .setMimeType("application/vnd.google-apps.folder");
        try {
            dir = mDrive.files()
                    .create(metadata)
                    .execute();

            Timber.d("Directory %s created", name);
            return dir.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    String createFile(String name, String parentId) {
        File driveFile = searchFile(name, parentId);
        if (driveFile != null) {
            Timber.d("File %s already exits", name);
            return driveFile.getId();
        }

        File metadata = new File()
                .setParents(Collections.singletonList(parentId))
                .setName(name)
                .setAppProperties(getProps())
                .setMimeType("application/json");
        try {
            driveFile = mDrive.files().create(metadata).execute();

            Timber.d("File %s created", name);
            return driveFile.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean updateFile(String fileId, String name, String type, InputStream content) {
        try {
            File metadata = new File()
                    .setName(name);
            mDrive.files().update(fileId, metadata, new InputStreamContent(type, content)).execute();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    InputStream getFileContent(String folder, String name) {
        File dir = searchFolder(folder);
        if (dir == null) {
            return null;
        }

        File file = searchFile(name, dir.getId());
        if (file == null) {
            return null;
        }
        try {
            return mDrive.files().get(file.getId()).executeMediaAsInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, String> getProps() {
        return Collections.singletonMap("mvasoft.timetracker.sync", "database_backup");
    }

    private File searchFolder(String name) {
        FileList result = null;
        try {
            String query = "trashed != true" +
                    String.format(" and name = '%s'", name) +
                    " and mimeType='application/vnd.google-apps.folder'";
            result = mDrive.files().list()
                    .setQ(query)
                    .execute();
            if (!result.getFiles().isEmpty())
                return result.getFiles().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File searchFile(String name, String dirId) {
        try {
            String query = "trashed != true" +
                    " and mimeType!='application/vnd.google-apps.folder'" +
                    String.format(" and name = '%s'", name) +
                    String.format(" and '%s' in parents", dirId) +
                    " and appProperties has {key='mvasoft.timetracker.sync' and value='database_backup'}";
            FileList result = mDrive.files().list()
                    .setQ(query)
                    .execute();
            for (File file : result.getFiles()) {
                Timber.d("Found files: %s", file.getName());
            }
            if (!result.getFiles().isEmpty())
                return result.getFiles().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
