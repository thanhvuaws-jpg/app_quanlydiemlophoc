package vn.edu.vaa.classmanagerdemo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "class_manager.db";
    public static final int DB_VERSION = 7;

    // ── users ──
    public static final String TABLE_USERS = "users";
    public static final String USER_ID = "id";
    public static final String USER_FULL_NAME = "fullName";
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_EMAIL = "email";
    public static final String USER_PHONE = "phone";
    public static final String USER_TRAINING_POINTS = "trainingPoints";

    // ── scores ──
    public static final String TABLE_SCORES = "scores";
    public static final String SCR_ID = "id";
    public static final String SCR_STUDENT_ID = "student_id";
    public static final String SCR_SUBJECT = "subject";
    public static final String SCR_CREDITS = "credits";
    public static final String SCR_SCORE_QT = "score_qt";
    public static final String SCR_WEIGHT_QT = "weight_qt";
    public static final String SCR_SCORE_CK = "score_ck";
    public static final String SCR_WEIGHT_CK = "weight_ck";
    public static final String SCR_VALUE = "score";
    public static final String SCR_SEMESTER = "semester";

    private static final String CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USER_FULL_NAME + " TEXT NOT NULL, " +
                    USER_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    USER_PASSWORD + " TEXT NOT NULL, " +
                    USER_EMAIL + " TEXT, " +
                    USER_PHONE + " TEXT, " +
                    USER_TRAINING_POINTS + " INTEGER DEFAULT 80);";

    private static final String CREATE_SCORES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SCORES + " (" +
                    SCR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SCR_STUDENT_ID + " INTEGER NOT NULL, " +
                    SCR_SUBJECT + " TEXT NOT NULL, " +
                    SCR_CREDITS + " INTEGER DEFAULT 3, " +
                    SCR_SCORE_QT + " REAL DEFAULT 0, " +
                    SCR_WEIGHT_QT + " INTEGER DEFAULT 50, " +
                    SCR_SCORE_CK + " REAL DEFAULT 0, " +
                    SCR_WEIGHT_CK + " INTEGER DEFAULT 50, " +
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
        db.execSQL(CREATE_SCORES);
        seedUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS students");
            db.execSQL("DROP TABLE IF EXISTS classes");
            db.execSQL("DROP TABLE IF EXISTS todos");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        } else if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + USER_TRAINING_POINTS + " INTEGER DEFAULT 80;");
            } catch (Exception e) {
                // If alteration fails, clear and recreate
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                onCreate(db);
            }
        }
    }

    private void seedUsers(SQLiteDatabase db) {
        String hashed = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS + " (" +
                USER_ID + ", " + USER_FULL_NAME + ", " + USER_USERNAME + ", " + USER_PASSWORD + ", " + USER_EMAIL + ", " + USER_PHONE + ", " + USER_TRAINING_POINTS + ") VALUES " +
                "(1, 'Quản trị viên', 'admin', '" + hashed + "', 'admin@vaa.edu.vn', '0900000000', 85)," +
                "(2, 'Giảng viên demo', 'teacher', '" + hashed + "', 'teacher@vaa.edu.vn', '0911111111', 75);");
    }
}
