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
import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.Score;

public class CsvExporter {

    public static File exportScores(Context context, List<Score> scores) throws IOException {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) dir = context.getFilesDir();
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "gpa_scores_export.csv");

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF'); // BOM for UTF-8 Excel support
            pw.println("Học kỳ,Tên môn học,Số tín chỉ,Điểm quá trình,Tỷ lệ QT,Điểm cuối kỳ,Tỷ lệ CK,Điểm tổng");
            for (Score s : scores) {
                pw.println("\"" + safe(s.getSemester()) + "\",\"" + safe(s.getSubject()) + "\"," + s.getCredits() + "," +
                        s.getScoreQT() + "," + s.getWeightQT() + "," + s.getScoreCK() + "," + s.getWeightCK() + "," + s.getScore());
            }
        }
        return file;
    }

    public static List<Score> importScores(File file, int studentId) throws Exception {
        List<Score> list = new ArrayList<>();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"))) {
            
            br.mark(4);
            int firstChar = br.read();
            if (firstChar != 0xFEFF) {
                br.reset();
            }

            String header = br.readLine();
            if (header == null) throw new Exception("File CSV trống");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 7) continue;

                String semester = unquote(parts[0]);
                String subject = unquote(parts[1]);
                int credits = Integer.parseInt(unquote(parts[2]).trim());
                float scoreQT = Float.parseFloat(unquote(parts[3]).trim());
                int weightQT = Integer.parseInt(unquote(parts[4]).trim());
                float scoreCK = Float.parseFloat(unquote(parts[5]).trim());
                int weightCK = Integer.parseInt(unquote(parts[6]).trim());

                Score s = new Score(studentId, subject, credits, scoreQT, weightQT, scoreCK, weightCK, semester);
                list.add(s);
            }
        }
        return list;
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

    private static String unquote(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\"\"", "\"");
    }
}
