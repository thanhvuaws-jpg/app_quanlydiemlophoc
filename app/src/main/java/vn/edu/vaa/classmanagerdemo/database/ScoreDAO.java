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
        v.put(DatabaseHelper.SCR_SUBJECT, score.getSubject());
        v.put(DatabaseHelper.SCR_CREDITS, score.getCredits());
        v.put(DatabaseHelper.SCR_SCORE_QT, score.getScoreQT());
        v.put(DatabaseHelper.SCR_WEIGHT_QT, score.getWeightQT());
        v.put(DatabaseHelper.SCR_SCORE_CK, score.getScoreCK());
        v.put(DatabaseHelper.SCR_WEIGHT_CK, score.getWeightCK());
        v.put(DatabaseHelper.SCR_VALUE, score.getScore());
        v.put(DatabaseHelper.SCR_SEMESTER, score.getSemester());
        return db.insert(DatabaseHelper.TABLE_SCORES, null, v);
    }

    public int update(Score score) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.SCR_SUBJECT, score.getSubject());
        v.put(DatabaseHelper.SCR_CREDITS, score.getCredits());
        v.put(DatabaseHelper.SCR_SCORE_QT, score.getScoreQT());
        v.put(DatabaseHelper.SCR_WEIGHT_QT, score.getWeightQT());
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

    public List<Score> getByStudentId(int studentId) {
        List<Score> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=? " +
                " ORDER BY " + DatabaseHelper.SCR_SEMESTER + ", " + DatabaseHelper.SCR_SUBJECT,
                new String[]{String.valueOf(studentId)});
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Score> getBySemester(int studentId, String semester) {
        List<Score> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=? AND " + DatabaseHelper.SCR_SEMESTER + "=? " +
                " ORDER BY " + DatabaseHelper.SCR_SUBJECT,
                new String[]{String.valueOf(studentId), semester});
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
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
        return avg;
    }

    public int getTotalCreditsByStudentId(int studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.SCR_CREDITS + ") FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=?",
                new String[]{String.valueOf(studentId)});
        int total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getInt(0);
        c.close();
        return total;
    }

    public float getCumulativeGpaByStudentId(int studentId) {
        List<Score> scores = getByStudentId(studentId);
        if (scores.isEmpty()) return 0f;
        float totalPoints = 0f;
        int totalCredits = 0;
        for (Score s : scores) {
            totalPoints += s.getGrade4() * s.getCredits();
            totalCredits += s.getCredits();
        }
        if (totalCredits == 0) return 0f;
        float gpa = totalPoints / totalCredits;
        return Math.round(gpa * 100f) / 100f; // Round to 2 decimal places
    }

    public int getTotalCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCORES, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getSubjectCountByStudentId(int studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCORES + " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=?", new String[]{String.valueOf(studentId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    private Score fromCursor(Cursor c) {
        return new Score(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_ID)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_STUDENT_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SUBJECT)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_CREDITS)),
                c.getFloat(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SCORE_QT)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_WEIGHT_QT)),
                c.getFloat(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SCORE_CK)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SCR_WEIGHT_CK)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SCR_SEMESTER))
        );
    }

    public boolean existsBySubjectAndSemester(int studentId, String subject, String semester) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_STUDENT_ID + "=?" +
                " AND " + DatabaseHelper.SCR_SUBJECT + "=?" +
                " AND " + DatabaseHelper.SCR_SEMESTER + "=?",
                new String[]{String.valueOf(studentId), subject, semester});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }
}
