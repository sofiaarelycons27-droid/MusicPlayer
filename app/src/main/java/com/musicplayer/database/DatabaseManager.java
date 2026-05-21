// database/DatabaseManager.java
package com.musicplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.musicplayer.models.Song;
import com.musicplayer.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // ==================== USER CRUD ====================

    public long registerUser(String email, String username, String password) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_EMAIL, email.toLowerCase());
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put(DatabaseHelper.COLUMN_JOIN_DATE, sdf.format(new Date()));

        return database.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public User loginUser(String email, String password) {
        String[] columns = {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_EMAIL,
                DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_JOIN_DATE};
        String selection = DatabaseHelper.COLUMN_EMAIL + " = ? AND " +
                DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email.toLowerCase(), password};

        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS, columns,
                selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    password,
                    cursor.getString(3)
            );
            cursor.close();
            return user;
        }
        return null;
    }

    public User getUserById(int userId) {
        String[] columns = {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_EMAIL,
                DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_JOIN_DATE};
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS, columns,
                selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    "",
                    cursor.getString(3)
            );
            cursor.close();
            return user;
        }
        return null;
    }

    public boolean deleteUser(int userId) {
        // Primero eliminar las canciones del usuario
        database.delete(DatabaseHelper.TABLE_SONGS,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});

        // Luego eliminar el usuario
        int rows = database.delete(DatabaseHelper.TABLE_USERS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // ==================== SONG CRUD ====================

    public long addSong(Song song) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, song.getTitle());
        values.put(DatabaseHelper.COLUMN_ARTIST, song.getArtist());
        values.put(DatabaseHelper.COLUMN_GENRE, song.getGenre());
        values.put(DatabaseHelper.COLUMN_DURATION, song.getDuration());
        values.put(DatabaseHelper.COLUMN_FILE_NAME, song.getFileName());
        values.put(DatabaseHelper.COLUMN_IS_FAVORITE, song.isFavorite() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_USER_ID, song.getUserId());

        return database.insert(DatabaseHelper.TABLE_SONGS, null, values);
    }

    public List<Song> getAllSongs(int userId) {
        List<Song> songs = new ArrayList<>();
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = database.query(DatabaseHelper.TABLE_SONGS, null,
                selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Song song = new Song(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ARTIST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FILE_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_FAVORITE)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID))
                );
                songs.add(song);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return songs;
    }

    public Song getSongById(int songId) {
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(songId)};

        Cursor cursor = database.query(DatabaseHelper.TABLE_SONGS, null,
                selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Song song = new Song(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ARTIST)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FILE_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_FAVORITE)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID))
            );
            cursor.close();
            return song;
        }
        return null;
    }

    public int updateSong(Song song) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, song.getTitle());
        values.put(DatabaseHelper.COLUMN_ARTIST, song.getArtist());
        values.put(DatabaseHelper.COLUMN_GENRE, song.getGenre());
        values.put(DatabaseHelper.COLUMN_DURATION, song.getDuration());
        values.put(DatabaseHelper.COLUMN_IS_FAVORITE, song.isFavorite() ? 1 : 0);

        String where = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(song.getId())};

        return database.update(DatabaseHelper.TABLE_SONGS, values, where, whereArgs);
    }

    public int deleteSong(int songId) {
        return database.delete(DatabaseHelper.TABLE_SONGS,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(songId)});
    }

    public List<Song> searchSongs(int userId, String query) {
        List<Song> songs = new ArrayList<>();
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND (" +
                DatabaseHelper.COLUMN_TITLE + " LIKE ? OR " +
                DatabaseHelper.COLUMN_ARTIST + " LIKE ?)";
        String[] selectionArgs = {String.valueOf(userId), "%" + query + "%", "%" + query + "%"};

        Cursor cursor = database.query(DatabaseHelper.TABLE_SONGS, null,
                selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Song song = new Song(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ARTIST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FILE_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_FAVORITE)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID))
                );
                songs.add(song);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return songs;
    }

    public List<String> getAllGenres(int userId) {
        List<String> genres = new ArrayList<>();
        genres.add("Todos");

        String query = "SELECT DISTINCT " + DatabaseHelper.COLUMN_GENRE +
                " FROM " + DatabaseHelper.TABLE_SONGS +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String genre = cursor.getString(0);
                if (!genres.contains(genre)) {
                    genres.add(genre);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return genres;
    }
}