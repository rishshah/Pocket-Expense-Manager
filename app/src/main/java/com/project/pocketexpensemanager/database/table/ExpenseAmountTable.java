package com.project.pocketexpensemanager.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class ExpenseAmountTable implements BaseColumns {

    public static final String TABLE_NAME = "expense_amount";
    public static final String COLUMN_EXPENSE_ID = "expense_id";
    public static final String COLUMN_MOP  = "method_of_payment";
    public static final String COLUMN_AMOUNT  = "amount";

    // Database creation SQL statement
    private static final String TABLE_CREATE = "create table " + TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_EXPENSE_ID + " INTEGER," +
            COLUMN_AMOUNT + " FLOAT," +
            COLUMN_MOP + " VARCHAR(20)," +
            "FOREIGN KEY("+ COLUMN_EXPENSE_ID +") REFERENCES " + ExpenseTable.TABLE_NAME + "(_id) ON UPDATE CASCADE," +
            "FOREIGN KEY("+ COLUMN_MOP +") REFERENCES " + ReserveTable.TABLE_NAME + "(" + ReserveTable.COLUMN_TYPE + ") ON UPDATE CASCADE" +
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
