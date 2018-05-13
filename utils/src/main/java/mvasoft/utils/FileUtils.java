package mvasoft.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

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

    public static boolean copyFile(File source, File dest) {
        if (source == null || dest == null || !source.exists())
            return false;

        try {
            //noinspection ResultOfMethodCallIgnored
            dest.getParentFile().mkdirs();

            FileInputStream input = null;
            FileOutputStream output = null;
            try {
                input = new FileInputStream(source);
                output = new FileOutputStream(dest);

                FileChannel src = input.getChannel();
                FileChannel out = output.getChannel();

                out.transferFrom(src, 0, src.size());
            }
            finally {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
