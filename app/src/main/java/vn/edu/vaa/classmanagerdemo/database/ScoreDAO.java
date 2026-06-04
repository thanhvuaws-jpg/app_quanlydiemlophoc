package vn.edu.vaa.classmanagerdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.Score;

public class ScoreDAO {
    private final DatabaseHelper dbHelper;

    public ScoreDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Score score) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.SCR_STUDENT_ID, score.getStudentId());
        v.put(DatabaseHelper.SCR_CLASS_ID, score.getClassId());
        v.put(DatabaseHelper.SCR_SUBJECT, score.getSubject());
        v.put(DatabaseHelper.SCR_SCORE_QT, score.getScoreQT());
        v.put(DatabaseHelper.SCR_WEIGHT_QT, score.getWeightQT());
        v.put(DatabaseHelper.SCR_SCORE_GK, score.getScoreGK());
        v.put(DatabaseHelper.SCR_WEIGHT_GK, score.getWeightGK());
        v.put(DatabaseHelper.SCR_SCORE_CK, score.getScoreCK());
        v.put(DatabaseHelper.SCR_WEIGHT_CK, score.getWeightCK());
        v.put(DatabaseHelper.SCR_VALUE, score.getScore());
        v.put(DatabaseHelper.SCR_SEMESTER, score.getSemester());
        return db.insert(DatabaseHelper.TABLE_SCORES, null, v);
    }

    public int update(Score score) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.SCR_SCORE_QT, score.getScoreQT());
        v.put(DatabaseHelper.SCR_WEIGHT_QT, score.getWeightQT());
        v.put(DatabaseHelper.SCR_SCORE_GK, score.getScoreGK());
        v.put(DatabaseHelper.SCR_WEIGHT_GK, score.getWeightGK());
        v.put(DatabaseHelper.SCR_SCORE_CK, score.getScoreCK());
        v.put(DatabaseHelper.SCR_WEIGHT_CK, score.getWeightCK());
        v.put(DatabaseHelper.SCR_VALUE, score.getScore());
        v.put(DatabaseHelper.SCR_SEMESTER, score.getSemester());
        return db.update(DatabaseHelper.TABLE_SCORES, v,
                DatabaseHelper.SCR_ID + "=?", new String[]{String.valueOf(score.getId())});
    }

    public int deleteById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SCORES,
                DatabaseHelper.SCR_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Lấy điểm theo studentId trong 1 lớp
    public List<Score> getByStudentAndClass(int studentId, int classId) {
        List<Score> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=?" +
                " AND " + DatabaseHelper.SCR_CLASS_ID + "=?" +
                " ORDER BY " + DatabaseHelper.SCR_SEMESTER,
                new String[]{String.valueOf(studentId), String.valueOf(classId)});
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // Lấy tất cả điểm trong 1 lớp (cho xuất Excel)
    public List<Score> getByClassId(int classId) {
        List<Score> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT sc.*, st." + DatabaseHelper.STU_FULL_NAME + " as studentName, st." + DatabaseHelper.STU_CODE + " as studentCode" +
                " FROM " + DatabaseHelper.TABLE_SCORES + " sc" +
                " JOIN " + DatabaseHelper.TABLE_STUDENTS + " st ON st." + DatabaseHelper.STU_ID + "=sc." + DatabaseHelper.SCR_STUDENT_ID +
                " WHERE sc." + DatabaseHelper.SCR_CLASS_ID + "=?" +
                " ORDER BY st." + DatabaseHelper.STU_FULL_NAME,
                new String[]{String.valueOf(classId)});
        if (c.moveToFirst()) {
            do {
                Score s = fromCursor(c);
                int nameIdx = c.getColumnIndex("studentName");
                int codeIdx = c.getColumnIndex("studentCode");
                if (nameIdx >= 0) s.setStudentName(c.getString(nameIdx));
                if (codeIdx >= 0) s.setStudentCode(c.getString(codeIdx));
                list.add(s);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean existsByStudentAndClass(int studentId, int classId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=?" +
                " AND " + DatabaseHelper.SCR_CLASS_ID + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(classId)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private Score fromCursor(Cursor c) {
        Score s = new Score();
        s.setId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_ID)));
        s.setStudentId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_STUDENT_ID)));
        s.setClassId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_CLASS_ID)));
        s.setSubject(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SUBJECT)));
        s.setScoreQT(c.getFloat(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SCORE_QT)));
        s.setWeightQT(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_WEIGHT_QT)));
        s.setScoreGK(c.getFloat(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SCORE_GK)));
        s.setWeightGK(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_WEIGHT_GK)));
        s.setScoreCK(c.getFloat(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SCORE_CK)));
        s.setWeightCK(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_WEIGHT_CK)));
        s.setSemester(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SEMESTER)));
        return s;
    }
}
