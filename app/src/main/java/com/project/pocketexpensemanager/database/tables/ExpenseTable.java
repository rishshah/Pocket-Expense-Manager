package com.project.pocketexpensemanager.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class ExpenseTable implements BaseColumns{

    public static final String TABLE_NAME = "expense";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DESCRIPTION  = "description";

    // Database creation SQL statement
    private static final String TABLE_CREATE = "create table " + TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_DATE + " DATETIME, " +
            COLUMN_CATEGORY + " VARCHAR(20), " +
            COLUMN_DESCRIPTION + " TEXT, " +
            "FOREIGN KEY("+ COLUMN_CATEGORY +") REFERENCES " + CategoryTable.TABLE_NAME + "(" + CategoryTable.COLUMN_TYPE + ") ON UPDATE CASCADE ON DELETE CASCADE" +
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