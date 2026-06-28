package com.example.projectscanotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "scanotes_db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_HISTORY = "history";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_EXPLANATION = "explanation";
    private static final String KEY_EXAMPLES = "examples";
    private static final String KEY_SUMMARY = "summary";
    private static final String KEY_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_EXPLANATION + " TEXT,"
                + KEY_EXAMPLES + " TEXT,"
                + KEY_SUMMARY + " TEXT,"
                + KEY_TIMESTAMP + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public long insertHistory(String title, String explanation, String examples, String summary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_EXPLANATION, explanation);
        values.put(KEY_EXAMPLES, examples);
        values.put(KEY_SUMMARY, summary);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | hh:mm a", Locale.ENGLISH);
        String currentDateAndTime = sdf.format(new Date());
        values.put(KEY_TIMESTAMP, currentDateAndTime);

        long id = db.insert(TABLE_HISTORY, null, values);
        db.close();
        return id;
    }

    public ArrayList<SavedNoteModel> getAllHistory() {
        ArrayList<SavedNoteModel> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + KEY_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SavedNoteModel model = new SavedNoteModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                );
                list.add(model);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public SavedNoteModel getLastHistory() {
        SavedNoteModel model = null;
        String selectQuery = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + KEY_ID + " DESC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            model = new SavedNoteModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
            );
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return model;
    }

    // ================= TAMBAHAN DI-UPDATE BARU =================
    // Ambil maksimal beberapa catatan (misal 3) yang paling baru aktif diakses/dibuat
    public ArrayList<SavedNoteModel> getLatestActiveHistory(int limit) {
        ArrayList<SavedNoteModel> list = new ArrayList<>();
        // Diurutkan berdasarkan kolom timestamp agar catatan yang baru dibuka langsung melompat ke atas
        String selectQuery = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + KEY_TIMESTAMP + " DESC LIMIT " + limit;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                SavedNoteModel model = new SavedNoteModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                );
                list.add(model);
            }  while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        db.close();
        return list;
    }

    // Memperbarui waktu akses catatan agar terhitung sebagai yang paling terakhir dibuka
    public void updateNoteTimestamp(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | hh:mm a", Locale.ENGLISH);
        String currentDateAndTime = sdf.format(new Date());
        values.put(KEY_TIMESTAMP, currentDateAndTime);

        db.update(TABLE_HISTORY, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}