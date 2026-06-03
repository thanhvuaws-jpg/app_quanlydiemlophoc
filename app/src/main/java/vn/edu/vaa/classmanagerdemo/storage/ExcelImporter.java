package vn.edu.vaa.classmanagerdemo.storage;

import android.content.Context;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExcelImporter {

    public static List<String[]> parse(Context context, Uri uri, String filename) throws Exception {
        InputStream is = context.getContentResolver().openInputStream(uri);
        if (is == null) throw new Exception("Không mở được file");
        String lower = filename.toLowerCase();
        if (lower.endsWith(".csv")) return parseCsv(is);
        if (lower.endsWith(".xlsx")) return parseXlsx(is);
        throw new Exception("Chỉ hỗ trợ file .csv hoặc .xlsx");
    }

    // ── CSV ──────────────────────────────────────────────────────────────────

    private static List<String[]> parseCsv(InputStream is) throws IOException {
        List<String[]> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                // strip UTF-8 BOM
                if (line.startsWith("﻿")) line = line.substring(1);
                first = false;
            }
            if (line.trim().isEmpty()) continue;
            result.add(splitCsvLine(line));
        }
        reader.close();
        return result;
    }

    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if ((ch == ',' || ch == '\t') && !inQuotes) {
                fields.add(sb.toString().trim());
                sb = new StringBuilder();
            } else {
                sb.append(ch);
            }
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }

    // ── XLSX ─────────────────────────────────────────────────────────────────
    // XLSX = ZIP archive; we read xl/sharedStrings.xml then xl/worksheets/sheet1.xml

    private static List<String[]> parseXlsx(InputStream is) throws Exception {
        byte[] bytes = toByteArray(is);

        List<String> sharedStrings = readSharedStrings(bytes);
        return readSheet(bytes, sharedStrings);
    }

    private static List<String> readSharedStrings(byte[] bytes) throws Exception {
        List<String> result = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("xl/sharedStrings.xml".equals(entry.getName())) {
                    XmlPullParser p = newParser(zis);
                    StringBuilder sb = new StringBuilder();
                    boolean inSi = false;
                    int ev;
                    while ((ev = p.next()) != XmlPullParser.END_DOCUMENT) {
                        String tag = p.getName();
                        if (ev == XmlPullParser.START_TAG) {
                            if ("si".equals(tag)) { inSi = true; sb = new StringBuilder(); }
                        } else if (ev == XmlPullParser.TEXT && inSi) {
                            sb.append(p.getText());
                        } else if (ev == XmlPullParser.END_TAG) {
                            if ("si".equals(tag)) { result.add(sb.toString()); inSi = false; }
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    private static List<String[]> readSheet(byte[] bytes, List<String> ss) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("xl/worksheets/sheet1.xml".equals(entry.getName())) {
                    XmlPullParser p = newParser(zis);
                    List<String> row = null;
                    String cellType = null;
                    StringBuilder vSb = new StringBuilder();
                    boolean inV = false;
                    int ev;
                    while ((ev = p.next()) != XmlPullParser.END_DOCUMENT) {
                        String tag = p.getName();
                        if (ev == XmlPullParser.START_TAG) {
                            if ("row".equals(tag)) {
                                row = new ArrayList<>();
                            } else if ("c".equals(tag)) {
                                cellType = p.getAttributeValue(null, "t");
                                vSb = new StringBuilder();
                                inV = false;
                            } else if ("v".equals(tag) || ("is".equals(tag))) {
                                inV = true;
                            }
                        } else if (ev == XmlPullParser.TEXT && inV) {
                            vSb.append(p.getText());
                        } else if (ev == XmlPullParser.END_TAG) {
                            if ("v".equals(tag)) inV = false;
                            else if ("c".equals(tag)) {
                                String val = "";
                                String raw = vSb.toString().trim();
                                if ("s".equals(cellType)) {
                                    try {
                                        int idx = Integer.parseInt(raw);
                                        val = idx < ss.size() ? ss.get(idx) : raw;
                                    } catch (NumberFormatException e) { val = raw; }
                                } else if (!raw.isEmpty()) {
                                    val = raw;
                                }
                                if (row != null) row.add(val);
                            } else if ("row".equals(tag) && row != null) {
                                rows.add(row.toArray(new String[0]));
                            }
                        }
                    }
                    break;
                }
            }
        }
        return rows;
    }

    private static XmlPullParser newParser(InputStream is) throws Exception {
        XmlPullParserFactory f = XmlPullParserFactory.newInstance();
        XmlPullParser p = f.newPullParser();
        p.setInput(is, "UTF-8");
        return p;
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = is.read(chunk)) != -1) buffer.write(chunk, 0, n);
        is.close();
        return buffer.toByteArray();
    }
}
