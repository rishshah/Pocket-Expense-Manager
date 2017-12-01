package com.project.pocketexpensemanager.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class TransferTable implements BaseColumns{

    private static final String TABLE_NAME = "transfer";

    // Database creation SQL statement
    private static final String TABLE_CREATE = "create table " + TABLE_NAME + " (" +
            "date DATETIME NOT NULL, " +
            "amount FLOAT NOT NULL," +
            "from_mode VARCHAR(20) NOT NULL," +
            "to_mode VARCHAR(20) NOT NULL," +
            "FOREIGN KEY(from_mode) REFERENCES reserve(type)," +
            "FOREIGN KEY(to_mode) REFERENCES reserve(type)" +
            ")";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(TABLE_NAME + " :", "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );
        onCreate(database);
    }

}