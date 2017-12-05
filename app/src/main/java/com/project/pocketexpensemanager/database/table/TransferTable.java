package com.project.pocketexpensemanager.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class TransferTable implements BaseColumns{

    public static final String TABLE_NAME = "transfer";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_FROM_MODE  = "from_mode";
    public static final String COLUMN_TO_MODE  = "to_mode";

    // Database creation SQL statement
    private static final String TABLE_CREATE = "create table " + TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_DATE + " DATETIME, " +
            COLUMN_AMOUNT + " FLOAT," +
            COLUMN_FROM_MODE + " VARCHAR(20)," +
            COLUMN_TO_MODE + " VARCHAR(20)," +
            "FOREIGN KEY("+ COLUMN_FROM_MODE +") REFERENCES " + ReserveTable.TABLE_NAME + "(" + ReserveTable.COLUMN_TYPE + ") ON UPDATE CASCADE," +
            "FOREIGN KEY("+ COLUMN_TO_MODE +") REFERENCES " + ReserveTable.TABLE_NAME + "(" + ReserveTable.COLUMN_TYPE + ") ON UPDATE CASCADE" +
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