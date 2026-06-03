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
        dbHelper = new DatabaseHelper(context);
    }

    public long insert(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, student.getName());
        values.put(DatabaseHelper.COL_CLASS, student.getClassName());
        values.put(DatabaseHelper.COL_EMAIL, student.getEmail());
        values.put(DatabaseHelper.COL_PHONE, student.getPhone());
        long id = db.insert(DatabaseHelper.TABLE_STUDENTS, null, values);
        db.close();
        return id;
    }

    public int update(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, student.getName());
        values.put(DatabaseHelper.COL_CLASS, student.getClassName());
        values.put(DatabaseHelper.COL_EMAIL, student.getEmail());
        values.put(DatabaseHelper.COL_PHONE, student.getPhone());
        int rows = db.update(DatabaseHelper.TABLE_STUDENTS, values,
                DatabaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(student.getId())});
        db.close();
        return rows;
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_STUDENTS,
                DatabaseHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Student> getAll() {
        return search("");
    }

    public List<Student> search(String keyword) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DatabaseHelper.TABLE_STUDENTS;
        String[] args = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " WHERE " + DatabaseHelper.COL_NAME + " LIKE ? OR " + DatabaseHelper.COL_CLASS + " LIKE ? OR " + DatabaseHelper.COL_STUDENT_CODE + " LIKE ?";
            String key = "%" + keyword.trim() + "%";
            args = new String[]{key, key, key};
        }
        sql += " ORDER BY " + DatabaseHelper.COL_NAME + " ASC";
        Cursor c = db.rawQuery(sql, args);
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public long insertFull(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, student.getName());
        values.put(DatabaseHelper.COL_CLASS, student.getClassName());
        values.put(DatabaseHelper.COL_EMAIL, student.getEmail());
        values.put(DatabaseHelper.COL_PHONE, student.getPhone());
        values.put(DatabaseHelper.COL_STUDENT_CODE, student.getStudentCode());
        values.put(DatabaseHelper.COL_CLASS_ID, student.getClassId());
        long id = db.insert(DatabaseHelper.TABLE_STUDENTS, null, values);
        db.close();
        return id;
    }

    public List<Student> getByClassId(int classId) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_STUDENTS +
                " WHERE " + DatabaseHelper.COL_CLASS_ID + "=?" +
                " ORDER BY " + DatabaseHelper.COL_NAME + " ASC",
                new String[]{String.valueOf(classId)});
        if (c.moveToFirst()) {
            do { list.add(fromCursor(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public boolean existsByStudentCode(String code) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + DatabaseHelper.TABLE_STUDENTS +
                " WHERE " + DatabaseHelper.COL_STUDENT_CODE + "=?",
                new String[]{code});
        boolean exists = c.moveToFirst();
        c.close();
        db.close();
        return exists;
    }

    private Student fromCursor(Cursor c) {
        Student s = new Student(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CLASS)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE))
        );
        int codeIdx = c.getColumnIndex(DatabaseHelper.COL_STUDENT_CODE);
        if (codeIdx >= 0) s.setStudentCode(c.getString(codeIdx));
        int classIdIdx = c.getColumnIndex(DatabaseHelper.COL_CLASS_ID);
        if (classIdIdx >= 0) s.setClassId(c.getInt(classIdIdx));
        return s;
    }
}
