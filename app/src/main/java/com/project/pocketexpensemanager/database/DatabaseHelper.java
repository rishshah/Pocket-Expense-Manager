package com.project.pocketexpensemanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.project.pocketexpensemanager.database.tables.CategoryTable;
import com.project.pocketexpensemanager.database.tables.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.tables.ExpenseTable;
import com.project.pocketexpensemanager.database.tables.LogTable;
import com.project.pocketexpensemanager.database.tables.ReserveTable;
import com.project.pocketexpensemanager.database.tables.TransferTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    public static final String DATABASE_NAME = "pem.db";
    public static final String MIME_TYPE = "application/x-sqlite3";
    private static final int DATABASE_VERSION = 1;
    //context
    private Context mContext;

    public static synchronized DatabaseHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        CategoryTable.onCreate(database);
        ReserveTable.onCreate(database);
        ExpenseTable.onCreate(database);
        TransferTable.onCreate(database);
        ExpenseAmountTable.onCreate(database);
        LogTable.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        CategoryTable.onUpgrade(database, oldVersion, newVersion);
        ReserveTable.onUpgrade(database, oldVersion, newVersion);
        ExpenseTable.onUpgrade(database, oldVersion, newVersion);
        TransferTable.onUpgrade(database, oldVersion, newVersion);
        ExpenseAmountTable.onUpgrade(database, oldVersion, newVersion);
        LogTable.onUpgrade(database, oldVersion, newVersion);
    }

}

