package vn.edu.vaa.classmanagerdemo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "class_manager.db";
    public static final int DB_VERSION = 4;

    // ── users ──
    public static final String TABLE_USERS = "users";
    public static final String USER_ID = "id";
    public static final String USER_FULL_NAME = "fullName";
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_EMAIL = "email";
    public static final String USER_PHONE = "phone";

    // ── students ──
    public static final String TABLE_STUDENTS = "students";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_CLASS = "className";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_STUDENT_CODE = "student_code";
    public static final String COL_CLASS_ID = "class_id";

    // ── classes ──
    public static final String TABLE_CLASSES = "classes";
    public static final String CLS_ID = "id";
    public static final String CLS_NAME = "name";
    public static final String CLS_YEAR = "school_year";

    // ── scores ──
    public static final String TABLE_SCORES = "scores";
    public static final String SCR_ID = "id";
    public static final String SCR_STUDENT_ID = "student_id";
    public static final String SCR_SUBJECT = "subject";
    public static final String SCR_VALUE = "score";
    public static final String SCR_SEMESTER = "semester";

    private static final String CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USER_FULL_NAME + " TEXT NOT NULL, " +
                    USER_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    USER_PASSWORD + " TEXT NOT NULL, " +
                    USER_EMAIL + " TEXT, " +
                    USER_PHONE + " TEXT);";

    private static final String CREATE_STUDENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_STUDENTS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_CLASS + " TEXT, " +
                    COL_EMAIL + " TEXT, " +
                    COL_PHONE + " TEXT, " +
                    COL_STUDENT_CODE + " TEXT, " +
                    COL_CLASS_ID + " INTEGER DEFAULT 0);";

    private static final String CREATE_CLASSES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CLASSES + " (" +
                    CLS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CLS_NAME + " TEXT NOT NULL UNIQUE, " +
                    CLS_YEAR + " TEXT);";

    private static final String CREATE_SCORES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SCORES + " (" +
                    SCR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SCR_STUDENT_ID + " INTEGER NOT NULL, " +
                    SCR_SUBJECT + " TEXT NOT NULL, " +
                    SCR_VALUE + " REAL NOT NULL, " +
                    SCR_SEMESTER + " TEXT);";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_STUDENTS);
        db.execSQL(CREATE_CLASSES);
        db.execSQL(CREATE_SCORES);
        seedUsers(db);
        seedStudents(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_USERS);
            seedUsers(db);
        }
        if (oldVersion < 3) {
            db.execSQL(CREATE_CLASSES);
            db.execSQL(CREATE_SCORES);
            try { db.execSQL("ALTER TABLE " + TABLE_STUDENTS + " ADD COLUMN " + COL_STUDENT_CODE + " TEXT"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_STUDENTS + " ADD COLUMN " + COL_CLASS_ID + " INTEGER DEFAULT 0"); } catch (Exception ignored) {}
        }
        if (oldVersion < 4) {
            // Rehash seed user passwords sang SHA-256
            String hashed = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
            db.execSQL("UPDATE " + TABLE_USERS + " SET " + USER_PASSWORD + "='" + hashed +
                    "' WHERE " + USER_USERNAME + " IN ('admin','teacher')");
        }
    }

    private void seedUsers(SQLiteDatabase db) {
        // SHA-256("123456") = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
        String hashed = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS + " (" +
                USER_ID + ", " + USER_FULL_NAME + ", " + USER_USERNAME + ", " + USER_PASSWORD + ", " + USER_EMAIL + ", " + USER_PHONE + ") VALUES " +
                "(1, 'Quản trị viên', 'admin', '" + hashed + "', 'admin@vaa.edu.vn', '0900000000')," +
                "(2, 'Giảng viên demo', 'teacher', '" + hashed + "', 'teacher@vaa.edu.vn', '0911111111');");
    }

    private void seedStudents(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_STUDENTS + " (" + COL_NAME + ", " + COL_CLASS + ", " + COL_EMAIL + ", " + COL_PHONE + ") VALUES " +
                "('Nguyễn Minh Triết', '21DHT01', 'trietnm@vaa.edu.vn', '0903123456')," +
                "('Lê Hoàng Yến', '21DHT01', 'yenlh@vaa.edu.vn', '0914987654')," +
                "('Phạm Đức Anh', '21DHT02', 'anhpd@vaa.edu.vn', '0988776655')," +
                "('Ngô Khánh Vy', '21DHT02', 'vynk@vaa.edu.vn', '0908112233')," +
                "('Đỗ Gia Bảo', '21DHT03', 'baodg@vaa.edu.vn', '0977443322');");
    }
}
