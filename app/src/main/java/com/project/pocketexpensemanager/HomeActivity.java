package com.project.pocketexpensemanager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.project.pocketexpensemanager.fragment.CreateExpense;
import com.project.pocketexpensemanager.fragment.CreateTransfer;
import com.project.pocketexpensemanager.fragment.SeeCategory;
import com.project.pocketexpensemanager.fragment.SeeExpensesAndTransfers;
import com.project.pocketexpensemanager.fragment.SeeReserve;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class HomeActivity extends DriveBase implements NavigationView.OnNavigationItemSelectedListener, Display {
    public static final int CREATE_EXPENSE = 1;
    public static final int CREATE_TRANSFER = 2;
    public static final int SEE_SUMMARY = 5;
    public static final int SEE_CATEGORY = 6;
    public static final int SEE_TRANSACTIONS = 7;
    public static final int SEE_RESERVE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Expenses");
        displayFragment(SEE_TRANSACTIONS);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setDriveResourceClient(Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this)));
        onDriveClientReady();
    }

    @Override
    public void displayFragment(int action) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (action) {
            case SEE_SUMMARY:
                break;
            case SEE_TRANSACTIONS:
                getSupportActionBar().setTitle("Recent Activities");
                fragment = new SeeExpensesAndTransfers();
                break;
            case SEE_RESERVE:
                getSupportActionBar().setTitle("Existing Reserves");
                fragment = new SeeReserve();
                break;
            case SEE_CATEGORY:
                getSupportActionBar().setTitle("Existing Categories");
                fragment = new SeeCategory();
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
            fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
        } else {
            showMessage("Work in progress...");
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_summary) {
            displayFragment(SEE_SUMMARY);
        }
        else if (id == R.id.nav_category) {
            displayFragment(SEE_CATEGORY);
        }
        else if (id == R.id.nav_reserve) {
            displayFragment(SEE_RESERVE);
        }
        else if (id == R.id.nav_settings) {
            //TODO Settings page
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createFileInAppFolder() {
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write("Hello World!");
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("New file")
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return getDriveResourceClient().createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(this, new OnSuccessListener<DriveFile>() {
                    @Override
                    public void onSuccess(DriveFile driveFile) {
                        showMessage("File created");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        showMessage("Unable to create file");
                    }
                });
    }

    @Override
    public void onDriveClientReady() {
        createFile();
        createFileInAppFolder();
    }

    private void createFile() {
        // [START create_file]
        final Task<DriveFolder> rootFolderTask = getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(Task<Void> task) throws Exception {
                        DriveFolder parent = rootFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write("Hello World!");
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("HelloWorld.txt")
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return getDriveResourceClient().createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                showMessage("file created");
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        showMessage("failed to create file");
                    }
                });
        // [END create_file]
    }
}
