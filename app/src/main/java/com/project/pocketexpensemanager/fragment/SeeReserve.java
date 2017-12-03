package com.project.pocketexpensemanager.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

public class SeeReserve extends Fragment {

    private Display mDisplay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.see_reserve, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        int[] adapterRowViews = new int[]{android.R.id.text1};
        Cursor reserveCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter reserveSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
                reserveCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        reserveSca.setDropDownViewResource(android.R.layout.simple_list_item_1);
        ((ListView) view.findViewById(R.id.reserve_list)).setAdapter(reserveSca);
        reserveCursor.close();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_create_reserve);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateReserveDialog();
            }
        });


        return view;
    }

    public void showCreateReserveDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_reserve, null);
        dialogBuilder.setView(dialogView);

        final EditText reserve = (EditText) dialogView.findViewById(R.id.reserve_text);

        dialogBuilder.setTitle("Create Reserve");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //TODO Save reserve in database
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDisplay = (Display) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCreatePostListener");
        }
    }

}
