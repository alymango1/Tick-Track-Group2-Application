package com.group2.practicenakakainis.Utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.group2.practicenakakainis.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    private static final String DATABASE_NAME = "TODO_DATABASE";
    private static final String TABLE_NAME = "TODO_TABLE";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "TASK";
    private static final String COL_3 = "STATUS";

    private static final String COL_4 = "COLOR";


    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, TASK TEXT, STATUS INTEGER, COLOR INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_4 + " INTEGER");
        }
    }

    public void insertTask(ToDoModel model) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2, model.getTask());
        values.put(COL_3, 0);
        values.put(COL_4, model.getColor());  // Save the color of the task
        db.insert(TABLE_NAME, null, values);
    }

    public void updateTask(int id, String task) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2, task);
        db.update(TABLE_NAME, values, COL_1 + "=?", new String[]{String.valueOf(id)});
    }

    public void updateStatus(int id, int status) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2, status);
        db.update(TABLE_NAME, values, "ID+?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(int id) {
        db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_1 + "=?", new String[]{String.valueOf(id)});
    }

    public void updateTaskColor(int id, int color) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_4, color);
        db.update(TABLE_NAME, values, COL_1 + "=?", new String[]{String.valueOf(id)});
    }


    @SuppressLint("Range")
    public List<ToDoModel> getAllTasks() {

        db = this.getWritableDatabase();
        Cursor cursor = null;
        List<ToDoModel> modelList = new ArrayList<>();

        db.beginTransaction();
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null) if (cursor.moveToFirst()) {
                do {
                    ToDoModel task = new ToDoModel();
                    task.setId(cursor.getInt(cursor.getColumnIndex(COL_1)));
                    task.setTask(cursor.getString(cursor.getColumnIndex(COL_2)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(COL_3)));
                    task.setColor(cursor.getInt(cursor.getColumnIndex(COL_4)));  // Load the color of the task
                    modelList.add(task);
                } while (cursor.moveToNext());
            }

        } finally {
            db.endTransaction();
            cursor.close();
        }
        return modelList;
    }
}