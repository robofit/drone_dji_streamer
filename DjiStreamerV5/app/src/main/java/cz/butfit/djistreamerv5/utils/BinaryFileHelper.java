package cz.butfit.djistreamerv5.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFileHelper {

    private File file;
    private BufferedOutputStream outputStream;

    // Constructor
    public BinaryFileHelper(Context context, String filename) {
        try {
            // Use context.getFilesDir() for internal storage or context.getExternalFilesDir() for external storage
            File directory = context.getExternalFilesDir(null);
            Log.d("File Path", "File path: " + directory.getAbsolutePath());
            file = new File(directory, filename);

            // Create FileOutputStream in append mode (true)
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            outputStream = new BufferedOutputStream(fileOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Write binary data to the file
    public void writeBinary(byte[] data) {
        try {
            if (outputStream != null) {
                outputStream.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close the output stream when done
    public void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
