package mvasoft.timetracker.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class FileUtils {


    public static File createTempFileInputStream(InputStream inputStream) {
        if (inputStream == null)
            return null;

        File destFile = null;
        try {
            destFile = File.createTempFile("temp_file", String.valueOf(System.currentTimeMillis()));
            destFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (destFile == null)
            return null;

        if (copyToFile(inputStream, destFile))
            return destFile;
        else
            return null;
    }

    static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            copyToFileOrThrow(inputStream, destFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    static void copyToFileOrThrow(InputStream inputStream, File destFile)
            throws IOException {
        if (destFile.exists()) {
            destFile.delete();
        }
        FileOutputStream out = new FileOutputStream(destFile);
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            out.flush();
            try {
                out.getFD().sync();
            } catch (IOException e) {
            }
            out.close();
        }
    }
}
