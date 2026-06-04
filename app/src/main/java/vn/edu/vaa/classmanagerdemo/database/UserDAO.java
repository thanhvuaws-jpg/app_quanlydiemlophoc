package vn.edu.vaa.classmanagerdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import vn.edu.vaa.classmanagerdemo.models.User;

public class UserDAO {
    private final DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long register(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.USER_FULL_NAME, user.getFullName());
        values.put(DatabaseHelper.USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.USER_PASSWORD, hashPassword(user.getPassword()));
        values.put(DatabaseHelper.USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.USER_PHONE, user.getPhone());
        // values.put(DatabaseHelper.USER_TRAINING_POINTS, user.getTrainingPoints());
        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + DatabaseHelper.USER_ID + " FROM " + DatabaseHelper.TABLE_USERS
                + " WHERE " + DatabaseHelper.USER_USERNAME + "=?", new String[]{username.trim()});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public User login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE " + DatabaseHelper.USER_USERNAME + "=? AND " + DatabaseHelper.USER_PASSWORD + "=?",
                new String[]{username.trim(), hashPassword(password)});
        User user = null;
        if (c.moveToFirst()) user = fromCursor(c);
        c.close();
        return user;
    }

    public User findById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS
                + " WHERE " + DatabaseHelper.USER_ID + "=?", new String[]{String.valueOf(id)});
        User user = null;
        if (c.moveToFirst()) user = fromCursor(c);
        c.close();
        return user;
    }

    public int updateTrainingPoints(int userId, int points) {
        // Training points are no longer stored in the new teacher DB schema
        return 0;
    }

    private User fromCursor(Cursor c) {
        return new User(
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.USER_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.USER_FULL_NAME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.USER_USERNAME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.USER_PASSWORD)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.USER_PHONE)),
                80 // Default training points as column removed
        );
    }

    static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
}
