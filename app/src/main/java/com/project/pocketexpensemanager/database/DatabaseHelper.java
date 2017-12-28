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

    private static final String DATABASE_NAME = "pem.db";
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

    public void importFromDrive() {
        String inFileName = "abc";
        final String outFileName = mContext.getDatabasePath(DATABASE_NAME).toString();
        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();

            Log.e("DatabaseHelper", "Import Completed");

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Unable to import database. Retry");
            e.printStackTrace();
        }
    }

    public void exportToDrive() {
        String outFileName = "xyz";
        //database path
        final String inFileName = mContext.getDatabasePath(DATABASE_NAME).toString();

        try {

            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();

            Log.e("DatabaseHelper", "Backup Completed");

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Unable to backup database. Retry");
            e.printStackTrace();
        }
    }
}

