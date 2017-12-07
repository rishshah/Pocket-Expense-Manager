package com.project.pocketexpensemanager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.project.pocketexpensemanager.constant.Constants;
import com.project.pocketexpensemanager.fragment.CreateExpense;
import com.project.pocketexpensemanager.fragment.CreateTransfer;
import com.project.pocketexpensemanager.fragment.SeeCategory;
import com.project.pocketexpensemanager.fragment.SeeExpenses;
import com.project.pocketexpensemanager.fragment.SeeReserve;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.text.ParseException;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Display {
    public static final int CREATE_EXPENSE = 1;
    public static final int CREATE_TRANSFER = 2;
    public static final int SEE_SUMMARY = 3;
    public static final int SEE_CATEGORY = 4;
    public static final int SEE_TRANSACTIONS = 5;
    public static final int SEE_RESERVE = 6;

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
    }

    @Override
    public void displayFragment(int action) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (action) {
            case SEE_SUMMARY:
                getSupportActionBar().setTitle("Summary");
                break;
            case SEE_TRANSACTIONS:
                getSupportActionBar().setTitle("Recent Activities");
                fragment = new SeeExpenses();
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
            displayFragment(SEE_TRANSACTIONS);
            //TODO Settings page
        }
        else if (id == R.id.nav_home) {
            displayFragment(SEE_TRANSACTIONS);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public String parseDate(String c){
        try {
            return Constants.OUTPUT_FORMAT.format(Constants.INPUT_FORMAT.parse(c));
        } catch (ParseException e) {
            Log.e("ERROR ", e.getMessage());
            return null;
        }
    }
}
