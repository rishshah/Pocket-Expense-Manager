package com.project.pocketexpensemanager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.fragments.SeeCategorywiseSummary;
import com.project.pocketexpensemanager.fragments.SeeDetailedSummary;
import com.project.pocketexpensemanager.fragments.SeeSettings;
import com.project.pocketexpensemanager.utilities.Constants;
import com.project.pocketexpensemanager.database.tables.ExpenseTable;
import com.project.pocketexpensemanager.fragments.CreateExpense;
import com.project.pocketexpensemanager.fragments.CreateTransfer;
import com.project.pocketexpensemanager.fragments.SeeCategory;
import com.project.pocketexpensemanager.fragments.SeeLog;
import com.project.pocketexpensemanager.fragments.SeeReserve;
import com.project.pocketexpensemanager.fragments.SeeExpense;
import com.project.pocketexpensemanager.fragments.communication.Display;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static com.project.pocketexpensemanager.LoginActivity.DRIVE_ID;
import static com.project.pocketexpensemanager.LoginActivity.SHARED_PREF_NAME;


public class HomeActivity extends DriveBase implements NavigationView.OnNavigationItemSelectedListener, Display {
    public static final int CREATE_EXPENSE = 1;
    public static final int CREATE_TRANSFER = 2;
    public static final int SEE_CATEGORY = 3;
    public static final int SEE_RESERVE = 4;
    public static final int SEE_SETTINGS = 5;
    public static final int SEE_LOG = 6;
    public static final int SEE_CATEGORYWISE_SUMMARY = 7;
    public static final int SEE_DETAILED_SUMMARY = 8;
    public static final int VIEW_PARTICULAR_EXPENSE = 9;

    public static final String EXPORT = "Export";
    public static final String IMPORT = "Import";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().getBooleanExtra(LoginActivity.NEWUSER, false)) {
            displayFragment(SEE_RESERVE);
        } else {
            displayFragment(SEE_LOG);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        ((TextView) nav.getHeaderView(0).findViewById(R.id.username)).setText(sharedPreferences.getString(LoginActivity.USERNAME, ""));
        ((TextView) nav.getHeaderView(0).findViewById(R.id.email)).setText(sharedPreferences.getString(LoginActivity.EMAIL, ""));
        nav.setNavigationItemSelectedListener(this);
    }

    @Override
    public void displayFragment(int action) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (action) {
            case SEE_CATEGORYWISE_SUMMARY:
                getSupportActionBar().setTitle("Detailed Monthly Summary");
                Calendar calendar = Calendar.getInstance();
                String currentMonth = Constants.MONTHS[calendar.get(Calendar.MONTH)];
                String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
                Bundle bundle = new Bundle();
                fragment = new SeeCategorywiseSummary();
                bundle.putString("month", currentMonth);
                bundle.putString("year", currentYear);
                fragment.setArguments(bundle);
                break;

            case SEE_DETAILED_SUMMARY:
                getSupportActionBar().setTitle("Detailed Monthly Summary");
                calendar = Calendar.getInstance();
                currentMonth = Constants.MONTHS[calendar.get(Calendar.MONTH)];
                currentYear = String.valueOf(calendar.get(Calendar.YEAR));
                bundle = new Bundle();
                fragment = new SeeDetailedSummary();
                bundle.putString("month", currentMonth);
                bundle.putString("year", currentYear);
                fragment.setArguments(bundle);
                break;

            case SEE_SETTINGS:
                NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
                ((TextView) nav.getHeaderView(0).findViewById(R.id.username)).setText(sharedPreferences.getString(LoginActivity.USERNAME, ""));
                ((TextView) nav.getHeaderView(0).findViewById(R.id.email)).setText(sharedPreferences.getString(LoginActivity.EMAIL, ""));

                getSupportActionBar().setTitle("Settings");
                bundle = new Bundle();
                fragment = new SeeSettings();
                bundle.putString(LoginActivity.USERNAME, getIntent().getStringExtra(LoginActivity.USERNAME));
                bundle.putString(LoginActivity.EMAIL, getIntent().getStringExtra(LoginActivity.EMAIL));
                fragment.setArguments(bundle);
                break;

            case SEE_LOG:
                getSupportActionBar().setTitle("Recent Activities");
                fragment = new SeeLog();
                break;
            case SEE_CATEGORY:
                getSupportActionBar().setTitle("Existing Categories");
                fragment = new SeeCategory();
                break;
            case SEE_RESERVE:
                getSupportActionBar().setTitle("Existing Reserves");
                fragment = new SeeReserve();
                break;
            case CREATE_EXPENSE:
                getSupportActionBar().setTitle("New Expense");
                fragment = new CreateExpense();
                break;
            case CREATE_TRANSFER:
                getSupportActionBar().setTitle("New Transfer");
                fragment = new CreateTransfer();
                break;
        }
        if (fragment != null) {
            fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(String.valueOf(action)).commit();
        }

    }

    @Override
    public void displayLinkedFragment(int action, Cursor cursor, Object data) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        switch (action) {
            case VIEW_PARTICULAR_EXPENSE:
                getSupportActionBar().setTitle(cursor.getString(3));
                fragment = new SeeExpense();
                bundle.putString("data", (String) data);
                bundle.putString("_id", cursor.getString(0));
                bundle.putString(ExpenseTable.COLUMN_DATE, cursor.getString(1));
                bundle.putString(ExpenseTable.COLUMN_CATEGORY, cursor.getString(2));
                bundle.putString(ExpenseTable.COLUMN_DESCRIPTION, cursor.getString(3));
                fragment.setArguments(bundle);
                break;

            case SEE_CATEGORYWISE_SUMMARY:
                getSupportActionBar().setTitle("Detailed Monthly Summary");
                String currentMonth = ((String[]) data)[0];
                String currentYear = ((String[]) data)[1];

                fragment = new SeeCategorywiseSummary();
                bundle.putString("month", currentMonth);
                bundle.putString("year", currentYear);
                fragment.setArguments(bundle);
                break;

            case SEE_DETAILED_SUMMARY:
                getSupportActionBar().setTitle("Detailed Monthly Summary");
                currentMonth = ((String[]) data)[0];
                currentYear = ((String[]) data)[1];

                fragment = new SeeDetailedSummary();
                bundle.putString("month", currentMonth);
                bundle.putString("year", currentYear);
                fragment.setArguments(bundle);
                break;
        }
        if (fragment != null) {
            fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(String.valueOf(action)).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finishAffinity();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_categorywise_summary) {
            displayFragment(SEE_CATEGORYWISE_SUMMARY);
        } else if (id == R.id.nav_detailed_summary) {
            displayFragment(SEE_DETAILED_SUMMARY);
        } else if (id == R.id.nav_category) {
            displayFragment(SEE_CATEGORY);
        } else if (id == R.id.nav_reserve) {
            displayFragment(SEE_RESERVE);
        } else if (id == R.id.nav_settings) {
            displayFragment(SEE_SETTINGS);
        } else if (id == R.id.nav_home) {
            displayFragment(SEE_LOG);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public String parseDate(String c) {
        try {
            return Constants.OUTPUT_FORMAT.format(Constants.INPUT_FORMAT.parse(c));
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public void onDriveClientReady(String action) {
        String driveIdString = sharedPreferences.getString(DRIVE_ID, "");
        if (action.equals(EXPORT)) {
            if (!driveIdString.equals("")) {
                updateBackup(driveIdString);
            } else {
                createBackup();
            }
        } else if (action.equals(IMPORT)) {
            DriveId id = DriveId.decodeFromString(driveIdString);
            retrieveContents(id.asDriveFile());
        }
    }

    public static void showMessage(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }

    private void createBackup() {
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(appFolderTask, createContentsTask).continueWithTask(new Continuation<Void, Task<DriveFile>>() {
            @Override
            public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                DriveFolder parent = appFolderTask.getResult();
                DriveContents contents = createContentsTask.getResult();
                writeContents(contents);
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(DatabaseHelper.DATABASE_NAME)
                        .setMimeType(DatabaseHelper.MIME_TYPE)
                        .setStarred(true)
                        .build();
                return getDriveResourceClient().createFile(parent, changeSet, contents);
            }
        }).addOnSuccessListener(this, new OnSuccessListener<DriveFile>() {
            @Override
            public void onSuccess(DriveFile driveFile) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(DRIVE_ID, driveFile.getDriveId().encodeToString());
                editor.apply();
                showMessage(getApplication(), "BackUp Done");
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(getApplication(), "Failed to create backup file. Retry later");
            }
        });
    }

    private void updateBackup(String driveIdString) {
        try {
            Task<DriveContents> openTask = getDriveResourceClient().openFile(DriveId.decodeFromString(driveIdString).asDriveFile(), DriveFile.MODE_READ_WRITE);
            openTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                @Override
                public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                    DriveContents driveContents = task.getResult();
                    writeContents(driveContents);

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setStarred(true)
                            .setLastViewedByMeDate(new Date())
                            .build();

                    return getDriveResourceClient().commitContents(driveContents, changeSet);
                }
            }).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    showMessage(getApplication(), "BackUp Updated");
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showMessage(getApplication(), "Failed to create backup file. Retry later");
                }
            });
        } catch (IllegalArgumentException e){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DRIVE_ID,"");
            editor.apply();
            showMessage(getApplication(), "Please try again ...");
        }
    }

    private void writeContents(DriveContents driveContents) {
        final String inFileName = getDatabasePath(DatabaseHelper.DATABASE_NAME).toString();

        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            ParcelFileDescriptor pfd = driveContents.getParcelFileDescriptor();
            OutputStream output = new FileOutputStream(pfd.getFileDescriptor());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            fis.close();
        } catch (IOException e) {
            showMessage(getApplication(), "Unable to backup database. Retry later");
        }
    }

    private void retrieveContents(DriveFile file) {
        Task<DriveContents> openFileTask = getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
        openFileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents contents = task.getResult();
                readContents(contents);
                showMessage(getApplication(), "Import Successful");
                return getDriveResourceClient().discardContents(contents);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(getApplication(), "Unable to read contents");
            }
        });
    }

    private void readContents(DriveContents contents) {
        final String outFileName = getDatabasePath(DatabaseHelper.DATABASE_NAME).toString();
        try {
            InputStream is = contents.getInputStream();
            OutputStream output = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            is.close();
        } catch (Exception e) {
            showMessage(getApplication(), "Unable to import database. Retry later");
            e.printStackTrace();
        }
    }
}
