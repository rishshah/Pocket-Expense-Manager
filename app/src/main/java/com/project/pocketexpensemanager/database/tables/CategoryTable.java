package com.project.pocketexpensemanager.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class CategoryTable implements BaseColumns{
    //Table name
    public static final String TABLE_NAME = "category";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_ACTIVE = "active";


    // Database creation SQL statement
    private static final String TABLE_CREATE = "create table " + TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_TYPE + " VARCHAR(20), " +
            COLUMN_ACTIVE + " INTEGER" +
            ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE);
        Log.e(TABLE_NAME, TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.e(TABLE_NAME + " :", "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );
        onCreate(database);
    }
}