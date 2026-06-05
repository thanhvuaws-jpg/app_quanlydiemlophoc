package vn.edu.vaa.classmanagerdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.SchoolClass;

public class ClassDAO {
    private final DatabaseHelper dbHelper;

    public ClassDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(SchoolClass c) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.CLS_TEACHER_ID, c.getTeacherId());
        v.put(DatabaseHelper.CLS_NAME, c.getClassName());
        v.put(DatabaseHelper.CLS_SUBJECT, c.getSubject());
        v.put(DatabaseHelper.CLS_SCHOOL_YEAR, c.getSchoolYear());
        return db.insert(DatabaseHelper.TABLE_CLASSES, null, v);
    }

    public int update(SchoolClass c) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.CLS_NAME, c.getClassName());
        v.put(DatabaseHelper.CLS_SUBJECT, c.getSubject());
        v.put(DatabaseHelper.CLS_SCHOOL_YEAR, c.getSchoolYear());
        return db.update(DatabaseHelper.TABLE_CLASSES, v,
                DatabaseHelper.CLS_ID + "=?", new String[]{String.valueOf(c.getId())});
    }

    public int deleteById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Xóa cascade: scores -> students -> class
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_SCORES +
                " WHERE " + DatabaseHelper.SCR_CLASS_ID + "=?", new Object[]{id});
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_STUDENTS +
                " WHERE " + DatabaseHelper.STU_CLASS_ID + "=?", new Object[]{id});
        return db.delete(DatabaseHelper.TABLE_CLASSES,
                DatabaseHelper.CLS_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<SchoolClass> getByTeacherId(int teacherId) {
        List<SchoolClass> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT c.*, COUNT(s.id) as student_count FROM " + DatabaseHelper.TABLE_CLASSES + " c " +
                "LEFT JOIN " + DatabaseHelper.TABLE_STUDENTS + " s ON s." + DatabaseHelper.STU_CLASS_ID + "=c." + DatabaseHelper.CLS_ID + " " +
                "WHERE c." + DatabaseHelper.CLS_TEACHER_ID + "=? " +
                "GROUP BY c." + DatabaseHelper.CLS_ID + " " +
                "ORDER BY c." + DatabaseHelper.CLS_SCHOOL_YEAR + " DESC, c." + DatabaseHelper.CLS_NAME,
                new String[]{String.valueOf(teacherId)});
        if (c.moveToFirst()) {
            do {
                SchoolClass sc = fromCursor(c);
                int idx = c.getColumnIndex("student_count");
                if (idx >= 0) sc.setStudentCount(c.getInt(idx));
                list.add(sc);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public int getTotalScoreCount(int teacherId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
            "SELECT COUNT(sc.id) FROM " + DatabaseHelper.TABLE_SCORES + " sc " +
            "JOIN " + DatabaseHelper.TABLE_CLASSES + " cl ON cl." + DatabaseHelper.CLS_ID + "=sc." + DatabaseHelper.SCR_CLASS_ID + " " +
            "WHERE cl." + DatabaseHelper.CLS_TEACHER_ID + "=?",
            new String[]{String.valueOf(teacherId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    private SchoolClass fromCursor(Cursor c) {
        return new SchoolClass(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CLS_ID)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CLS_TEACHER_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CLS_NAME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CLS_SUBJECT)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CLS_SCHOOL_YEAR))
        );
    }
}
