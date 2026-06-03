package vn.edu.vaa.classmanagerdemo.storage;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TextFileManager {
    public static void writeText(Context context, String fileName, String content) throws IOException {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8")) {
            writer.write(content);
        }
    }

    public static void appendText(Context context, String fileName, String content) throws IOException {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
             OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8")) {
            writer.write(content);
        }
    }

    public static String readText(Context context, String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
