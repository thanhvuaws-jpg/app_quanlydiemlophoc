package vn.edu.vaa.classmanagerdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.ClassRoom;

public class ClassDAO {
    private final DatabaseHelper dbHelper;

    public ClassDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insert(ClassRoom classRoom) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.CLS_NAME, classRoom.getName());
        v.put(DatabaseHelper.CLS_YEAR, classRoom.getSchoolYear());
        long id = db.insert(DatabaseHelper.TABLE_CLASSES, null, v);
        db.close();
        return id;
    }

    public int update(ClassRoom classRoom) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.CLS_NAME, classRoom.getName());
        v.put(DatabaseHelper.CLS_YEAR, classRoom.getSchoolYear());
        int rows = db.update(DatabaseHelper.TABLE_CLASSES, v,
                DatabaseHelper.CLS_ID + "=?", new String[]{String.valueOf(classRoom.getId())});
        db.close();
        return rows;
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_CLASSES,
                DatabaseHelper.CLS_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<ClassRoom> getAll() {
        List<ClassRoom> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT c.*, COUNT(s." + DatabaseHelper.COL_ID + ") as cnt " +
                "FROM " + DatabaseHelper.TABLE_CLASSES + " c " +
                "LEFT JOIN " + DatabaseHelper.TABLE_STUDENTS + " s ON s." + DatabaseHelper.COL_CLASS_ID + " = c." + DatabaseHelper.CLS_ID + " " +
                "GROUP BY c." + DatabaseHelper.CLS_ID + " ORDER BY c." + DatabaseHelper.CLS_NAME + " ASC", null);
        if (c.moveToFirst()) {
            do {
                ClassRoom cr = fromCursor(c);
                cr.setStudentCount(c.getInt(c.getColumnIndexOrThrow("cnt")));
                list.add(cr);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public ClassRoom getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE " + DatabaseHelper.CLS_ID + "=?",
                new String[]{String.valueOf(id)});
        ClassRoom cr = null;
        if (c.moveToFirst()) cr = fromCursor(c);
        c.close();
        db.close();
        return cr;
    }

    public ClassRoom getByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_CLASSES + " WHERE " + DatabaseHelper.CLS_NAME + "=?",
                new String[]{name});
        ClassRoom cr = null;
        if (c.moveToFirst()) cr = fromCursor(c);
        c.close();
        db.close();
        return cr;
    }

    public ClassRoom getOrCreate(String name, String schoolYear) {
        ClassRoom existing = getByName(name);
        if (existing != null) return existing;
        ClassRoom cr = new ClassRoom(name, schoolYear);
        long id = insert(cr);
        cr.setId((int) id);
        return cr;
    }

    public int getTotalStudentCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_STUDENTS +
                " WHERE " + DatabaseHelper.COL_CLASS_ID + " > 0", null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }

    private ClassRoom fromCursor(Cursor c) {
        return new ClassRoom(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CLS_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CLS_NAME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CLS_YEAR))
        );
    }
}
