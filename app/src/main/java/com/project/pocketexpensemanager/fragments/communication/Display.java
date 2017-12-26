package com.project.pocketexpensemanager.fragments.communication;

import android.database.Cursor;

public interface Display {
    void displayFragment(int action);
    void displayLinkedFragment(int action, Cursor cursor, String data);
    String parseDate(String date);
}
