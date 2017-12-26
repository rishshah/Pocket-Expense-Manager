package com.project.pocketexpensemanager.utilities;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.tables.LogTable;

import java.util.HashSet;
import java.util.Set;


public class LogItemAdapter extends CursorAdapter {
    private LayoutInflater cursorInflater;
    private String[] adapterColViews;
    private int[] adapterRowViews;
    private int logLayout;
    private int deletdedImg, updatedImg, latestImg;
    private Set<String> deletedItems, updatedItems, latestItems;

    // Default constructor
    public LogItemAdapter(Activity activity, int logLayout, Cursor logCursor, String[] adapterColViews, int[] adapterRowViews, int flags) {
        super(activity, logCursor, flags);
        cursorInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.adapterColViews = adapterColViews;
        this.adapterRowViews = adapterRowViews;
        this.logLayout = logLayout;
        deletdedImg = activity.getResources().getIdentifier("android:drawable/ic_delete", null, null);
        updatedImg = activity.getResources().getIdentifier("com.project.pocketexpensemanager:drawable/ic_update", null, null);
        latestImg = activity.getResources().getIdentifier("com.project.pocketexpensemanager:drawable/ic_latest", null, null);
        deletedItems = new HashSet<>();
        updatedItems = new HashSet<>();
        latestItems = new HashSet<>();
    }

    public void bindView(View view, Context context, Cursor cursor) {
        String status = cursor.getString(cursor.getColumnIndex(LogTable.COLUMN_DESCRIPTION_SUB));
        String id = cursor.getString(cursor.getColumnIndex(LogTable.COLUMN_HIDDEN_ID));
        String type = cursor.getString(cursor.getColumnIndex(LogTable.COLUMN_TYPE));
        String log_id = cursor.getString(cursor.getColumnIndex("_id"));
        String combination = id + ":-:" + type;

        ImageView statusPic = (ImageView) view.findViewById(R.id.status);
        if (status.contains("Deleted")) {
            if (!deletedItems.contains(combination))
                deletedItems.add(combination);
        } else if (status.contains("Updated")) {
            if (!updatedItems.contains(combination) && !deletedItems.contains(combination)) {
                updatedItems.add(combination);
                latestItems.add(log_id);
            }
        }

        for (int i = 0; i < adapterRowViews.length; i++) {
            TextView textView = (TextView) view.findViewById(adapterRowViews[i]);
            textView.setText(cursor.getString(cursor.getColumnIndex(adapterColViews[i])));
        }


        if (deletedItems.contains(combination)) {
            statusPic.setImageResource(deletdedImg);
        } else if (latestItems.contains(log_id)) {
            statusPic.setImageResource(latestImg);
        } else if (updatedItems.contains(combination)) {
            statusPic.setImageResource(updatedImg);
        } else {
            statusPic.setImageResource(latestImg);
        }

        Log.e("DeleteList", deletedItems.toString());
        Log.e("UpdateList", updatedItems.toString());
        Log.e("LatestList", latestItems.toString());
        Log.e("LogId", log_id);
    }


    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(logLayout, parent, false);
    }
}