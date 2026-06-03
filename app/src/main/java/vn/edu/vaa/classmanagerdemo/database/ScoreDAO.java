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
        dbHelper = new DatabaseHelper(context);
    }

    public long insert(Score score) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.SCR_STUDENT_ID, score.getStudentId());
        v.put(DatabaseHelper.SCR_SUBJECT, score.getSubject());
        v.put(DatabaseHelper.SCR_VALUE, score.getScore());
        v.put(DatabaseHelper.SCR_SEMESTER, score.getSemester());
        long id = db.insert(DatabaseHelper.TABLE_SCORES, null, v);
        db.close();
        return id;
    }

    public int update(Score score) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.SCR_SUBJECT, score.getSubject());
        v.put(DatabaseHelper.SCR_VALUE, score.getScore());
        v.put(DatabaseHelper.SCR_SEMESTER, score.getSemester());
        int rows = db.update(DatabaseHelper.TABLE_SCORES, v,
                DatabaseHelper.SCR_ID + "=?", new String[]{String.valueOf(score.getId())});
        db.close();
        return rows;
    }

    public int deleteById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_SCORES,
                DatabaseHelper.SCR_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Score> getByStudentId(int studentId) {
        List<Score> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT sc.*, s." + DatabaseHelper.COL_NAME + " as sname " +
                "FROM " + DatabaseHelper.TABLE_SCORES + " sc " +
                "LEFT JOIN " + DatabaseHelper.TABLE_STUDENTS + " s ON s." + DatabaseHelper.COL_ID + " = sc." + DatabaseHelper.SCR_STUDENT_ID + " " +
                "WHERE sc." + DatabaseHelper.SCR_STUDENT_ID + "=? " +
                "ORDER BY sc." + DatabaseHelper.SCR_SEMESTER + ", sc." + DatabaseHelper.SCR_SUBJECT,
                new String[]{String.valueOf(studentId)});
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public List<Score> getByClassId(int classId) {
        List<Score> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT sc.*, s." + DatabaseHelper.COL_NAME + " as sname " +
                "FROM " + DatabaseHelper.TABLE_SCORES + " sc " +
                "JOIN " + DatabaseHelper.TABLE_STUDENTS + " s ON s." + DatabaseHelper.COL_ID + " = sc." + DatabaseHelper.SCR_STUDENT_ID + " " +
                "WHERE s." + DatabaseHelper.COL_CLASS_ID + "=? " +
                "ORDER BY s." + DatabaseHelper.COL_NAME + ", sc." + DatabaseHelper.SCR_SUBJECT,
                new String[]{String.valueOf(classId)});
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public float getAverageByStudentId(int studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT AVG(" + DatabaseHelper.SCR_VALUE + ") FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=?",
                new String[]{String.valueOf(studentId)});
        float avg = 0f;
        if (c.moveToFirst() && !c.isNull(0)) avg = c.getFloat(0);
        c.close();
        db.close();
        return avg;
    }

    public int getTotalCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCORES, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }

    private Score fromCursor(Cursor c) {
        Score s = new Score(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_ID)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_STUDENT_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SUBJECT)),
                c.getFloat(c.getColumnIndexOrThrow(DatabaseHelper.SCR_VALUE)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SEMESTER))
        );
        int snameIdx = c.getColumnIndex("sname");
        if (snameIdx >= 0) s.setStudentName(c.getString(snameIdx));
        return s;
    }
}
