package cz.butfit.djistreamerv5.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {

    private File file;
    private BufferedWriter writer;

    // Constructor
    public FileHelper(Context context, String filename) {
        try {
            // Use context.getFilesDir() for internal storage or context.getExternalFilesDir() for external storage
            File directory = context.getExternalFilesDir(null);
            Log.d("File Path", "File path: " + directory.getAbsolutePath());
            file = new File(directory, filename);


            // Create FileWriter in append mode (true)
            FileWriter fileWriter = new FileWriter(file, true);
            writer = new BufferedWriter(fileWriter);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Write a line to the file
    public void writeLine(String line) {
        try {
            writer.write(line);
            writer.newLine(); // Writes a newline character
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close the writer when done
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}