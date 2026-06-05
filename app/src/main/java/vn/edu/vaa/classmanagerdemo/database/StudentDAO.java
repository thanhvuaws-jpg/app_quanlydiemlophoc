package vn.edu.vaa.classmanagerdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.Student;

public class StudentDAO {
    private final DatabaseHelper dbHelper;

    public StudentDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Student s) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.STU_CLASS_ID, s.getClassId());
        v.put(DatabaseHelper.STU_CODE, s.getStudentCode());
        v.put(DatabaseHelper.STU_FULL_NAME, s.getFullName());
        v.put(DatabaseHelper.STU_NOTE, s.getNote());
        return db.insert(DatabaseHelper.TABLE_STUDENTS, null, v);
    }

    public int update(Student s) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.STU_CODE, s.getStudentCode());
        v.put(DatabaseHelper.STU_FULL_NAME, s.getFullName());
        v.put(DatabaseHelper.STU_NOTE, s.getNote());
        return db.update(DatabaseHelper.TABLE_STUDENTS, v,
                DatabaseHelper.STU_ID + "=?", new String[]{String.valueOf(s.getId())});
    }

    public int deleteById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_SCORES,
                DatabaseHelper.SCR_STUDENT_ID + "=?", new String[]{String.valueOf(id)});
        return db.delete(DatabaseHelper.TABLE_STUDENTS,
                DatabaseHelper.STU_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<Student> getByClassId(int classId) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_STUDENTS +
                " WHERE " + DatabaseHelper.STU_CLASS_ID + "=?" +
                " ORDER BY " + DatabaseHelper.STU_FULL_NAME,
                new String[]{String.valueOf(classId)});
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean existsByCode(int classId, String code) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + DatabaseHelper.TABLE_STUDENTS +
                " WHERE " + DatabaseHelper.STU_CLASS_ID + "=? AND " + DatabaseHelper.STU_CODE + "=?",
                new String[]{String.valueOf(classId), code});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private Student fromCursor(Cursor c) {
        Student student = new Student(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.STU_ID)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.STU_CLASS_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.STU_CODE)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.STU_FULL_NAME))
        );
        int noteIdx = c.getColumnIndex(DatabaseHelper.STU_NOTE);
        if (noteIdx >= 0) student.setNote(c.getString(noteIdx));
        return student;
    }
}
