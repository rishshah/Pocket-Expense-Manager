package com.project.pocketexpensemanager.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class LogTable implements BaseColumns{

    public static final String TABLE_NAME = "log";
    public static final String COLUMN_LOG_DATE = "log_date";
    public static final String COLUMN_DESCRIPTION_MAIN  = "description_main";
    public static final String COLUMN_DESCRIPTION_SUB  = "description_sub";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_HIDDEN_ID = "hidden";
    public static final String COLUMN_EVENT_DATE = "event_date";

    // Database creation SQL statement
    private static final String TABLE_CREATE = "create table " + TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_EVENT_DATE + " DATETIME, " +
            COLUMN_LOG_DATE + " DATETIME, " +
            COLUMN_DESCRIPTION_MAIN + " TEXT, " +
            COLUMN_TYPE + " TEXT, " +
            COLUMN_DESCRIPTION_SUB + " TEXT, " +
            COLUMN_AMOUNT + " FLOAT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_HIDDEN_ID + " INTEGER, " +
            COLUMN_STATUS + " INTEGER " +
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