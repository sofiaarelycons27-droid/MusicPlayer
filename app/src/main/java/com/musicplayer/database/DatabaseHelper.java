// database/DatabaseHelper.java
package com.musicplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "musicplayer.db";
    private static final int DATABASE_VERSION = 1;

    // Tablas
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SONGS = "songs";

    // Columnas comunes
    public static final String COLUMN_ID = "id";

    // Columnas Users
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_JOIN_DATE = "join_date";

    // Columnas Songs
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_GENRE = "genre";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_FILE_NAME = "file_name";
    public static final String COLUMN_IS_FAVORITE = "is_favorite";
    public static final String COLUMN_USER_ID = "user_id";

    // Crear tabla Users
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_JOIN_DATE + " TEXT NOT NULL)";

    // Crear tabla Songs
    private static final String CREATE_TABLE_SONGS =
            "CREATE TABLE " + TABLE_SONGS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_ARTIST + " TEXT NOT NULL, " +
                    COLUMN_GENRE + " TEXT NOT NULL, " +
                    COLUMN_DURATION + " TEXT NOT NULL, " +
                    COLUMN_FILE_NAME + " TEXT NOT NULL, " +
                    COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_SONGS);
        insertDefaultSongs(db);
    }

    private void insertDefaultSongs(SQLiteDatabase db) {
        // Canciones de ejemplo para el usuario por defecto
        String insertSong = "INSERT INTO " + TABLE_SONGS +
                "(" + COLUMN_TITLE + "," + COLUMN_ARTIST + "," + COLUMN_GENRE +
                "," + COLUMN_DURATION + "," + COLUMN_FILE_NAME + "," + COLUMN_IS_FAVORITE +
                "," + COLUMN_USER_ID + ") VALUES (?,?,?,?,?,?,?)";

        Object[][] songs = {
                {"Bohemian Rhapsody", "Queen", "Rock", "5:55", "bohemian", 1, 1},
                {"Shape of You", "Ed Sheeran", "Pop", "3:53", "shape", 0, 1},
                {"Billie Jean", "Michael Jackson", "Pop", "4:54", "billie", 1, 1},
                {"Stairway to Heaven", "Led Zeppelin", "Rock", "8:02", "stairway", 0, 1}
        };

        for (Object[] song : songs) {
            db.execSQL(insertSong, song);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}