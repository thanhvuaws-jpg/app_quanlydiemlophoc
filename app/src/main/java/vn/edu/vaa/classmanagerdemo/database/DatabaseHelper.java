package vn.edu.vaa.classmanagerdemo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "so_diem.db";
    public static final int DB_VERSION = 2;

    // ── users (giáo viên) ──
    public static final String TABLE_USERS = "users";
    public static final String USER_ID = "id";
    public static final String USER_FULL_NAME = "fullName";
    public static final String USER_USERNAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_EMAIL = "email";
    public static final String USER_PHONE = "phone";

    // ── classes (lớp học) ──
    public static final String TABLE_CLASSES = "classes";
    public static final String CLS_ID = "id";
    public static final String CLS_TEACHER_ID = "teacher_id";
    public static final String CLS_NAME = "class_name";
    public static final String CLS_SUBJECT = "subject";
    public static final String CLS_SCHOOL_YEAR = "school_year";

    // ── students (học sinh) ──
    public static final String TABLE_STUDENTS = "students";
    public static final String STU_ID = "id";
    public static final String STU_CLASS_ID = "class_id";
    public static final String STU_CODE = "student_code";
    public static final String STU_FULL_NAME = "full_name";
    public static final String STU_NOTE = "note";

    // ── scores (điểm) ──
    public static final String TABLE_SCORES = "scores";
    public static final String SCR_ID = "id";
    public static final String SCR_STUDENT_ID = "student_id";
    public static final String SCR_CLASS_ID = "class_id";
    public static final String SCR_SUBJECT = "subject";
    public static final String SCR_SCORE_QT = "score_qt";
    public static final String SCR_WEIGHT_QT = "weight_qt";
    public static final String SCR_SCORE_GK = "score_gk";
    public static final String SCR_WEIGHT_GK = "weight_gk";
    public static final String SCR_SCORE_CK = "score_ck";
    public static final String SCR_WEIGHT_CK = "weight_ck";
    public static final String SCR_VALUE = "score";
    public static final String SCR_SEMESTER = "semester";

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
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_FULL_NAME + " TEXT NOT NULL, " +
                USER_USERNAME + " TEXT NOT NULL UNIQUE, " +
                USER_PASSWORD + " TEXT NOT NULL, " +
                USER_EMAIL + " TEXT, " +
                USER_PHONE + " TEXT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CLASSES + " (" +
                CLS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CLS_TEACHER_ID + " INTEGER NOT NULL, " +
                CLS_NAME + " TEXT NOT NULL, " +
                CLS_SUBJECT + " TEXT NOT NULL, " +
                CLS_SCHOOL_YEAR + " TEXT NOT NULL);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_STUDENTS + " (" +
                STU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STU_CLASS_ID + " INTEGER NOT NULL, " +
                STU_CODE + " TEXT NOT NULL, " +
                STU_FULL_NAME + " TEXT NOT NULL, " +
                STU_NOTE + " TEXT DEFAULT '');");


        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SCORES + " (" +
                SCR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SCR_STUDENT_ID + " INTEGER NOT NULL, " +
                SCR_CLASS_ID + " INTEGER NOT NULL, " +
                SCR_SUBJECT + " TEXT, " +
                SCR_SCORE_QT + " REAL DEFAULT 0, " +
                SCR_WEIGHT_QT + " INTEGER DEFAULT 20, " +
                SCR_SCORE_GK + " REAL DEFAULT 0, " +
                SCR_WEIGHT_GK + " INTEGER DEFAULT 30, " +
                SCR_SCORE_CK + " REAL DEFAULT 0, " +
                SCR_WEIGHT_CK + " INTEGER DEFAULT 50, " +
                SCR_VALUE + " REAL NOT NULL, " +
                SCR_SEMESTER + " TEXT);");

        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_STUDENTS + " ADD COLUMN " + STU_NOTE + " TEXT DEFAULT ''");
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                onCreate(db);
            }
        }
    }

    private void seedData(SQLiteDatabase db) {
        String hashed = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS +
                " (id, fullName, username, password, email, phone) VALUES " +
                "(1, 'Nguyễn Văn An', 'giaovien', '" + hashed + "', 'gv@school.edu.vn', '0900000001')");
    }
}
