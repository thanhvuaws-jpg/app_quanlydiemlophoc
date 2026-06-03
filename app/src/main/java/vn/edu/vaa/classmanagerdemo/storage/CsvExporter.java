package vn.edu.vaa.classmanagerdemo.storage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.Student;

public class CsvExporter {
    public static File exportStudents(Context context, List<Student> students) throws IOException {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) dir = context.getFilesDir();
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "students_export.csv");

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF');
            pw.println("id,name,className,email,phone");
            for (Student s : students) {
                pw.println(s.getId() + ",\"" + safe(s.getName()) + "\",\"" + safe(s.getClassName()) + "\",\"" +
                        safe(s.getEmail()) + "\",\"" + safe(s.getPhone()) + "\"");
            }
        }
        return file;
    }

    public static String readCsv(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public static Intent buildShareIntent(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(intent, "Chia sẻ file CSV");
    }

    private static String safe(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
