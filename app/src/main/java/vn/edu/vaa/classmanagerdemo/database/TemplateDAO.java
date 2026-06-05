package vn.edu.vaa.classmanagerdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.models.ScoreTemplate;

public class TemplateDAO {
    private final DatabaseHelper dbHelper;

    public TemplateDAO(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(ScoreTemplate t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TPL_TEACHER_ID, t.getTeacherId());
        cv.put(DatabaseHelper.TPL_NAME, t.getTemplateName());
        cv.put(DatabaseHelper.TPL_W_QT, t.getWeightQt());
        cv.put(DatabaseHelper.TPL_W_GK, t.getWeightGk());
        cv.put(DatabaseHelper.TPL_W_CK, t.getWeightCk());
        return db.insert(DatabaseHelper.TABLE_TEMPLATES, null, cv);
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_TEMPLATES, DatabaseHelper.TPL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<ScoreTemplate> getByTeacher(int teacherId) {
        List<ScoreTemplate> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.TPL_TEACHER_ID + "=?";
        String[] selectionArgs = {String.valueOf(teacherId)};
        Cursor cursor = db.query(DatabaseHelper.TABLE_TEMPLATES, null, selection, selectionArgs, null, null, DatabaseHelper.TPL_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                ScoreTemplate t = new ScoreTemplate();
                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TPL_ID)));
                t.setTeacherId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TPL_TEACHER_ID)));
                t.setTemplateName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TPL_NAME)));
                t.setWeightQt(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TPL_W_QT)));
                t.setWeightGk(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TPL_W_GK)));
                t.setWeightCk(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TPL_W_CK)));
                list.add(t);
            }
            cursor.close();
        }
        return list;
    }
}
