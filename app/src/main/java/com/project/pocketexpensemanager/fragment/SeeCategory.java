package com.project.pocketexpensemanager.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.CategoryTable;

public class SeeCategory extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.see_category, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        int[] adapterRowViews=new int[]{android.R.id.text1};
        Cursor categoryCursor = mDb.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter categorySca=new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
                categoryCursor, new String[]{CategoryTable.COLUMN_TYPE}, adapterRowViews,0);
        categorySca.setDropDownViewResource(android.R.layout.simple_list_item_1);
        ((ListView) view.findViewById(R.id.category_list)).setAdapter(categorySca);
        categoryCursor.close();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_create_category);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Display create see_category dialog box
            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

}
