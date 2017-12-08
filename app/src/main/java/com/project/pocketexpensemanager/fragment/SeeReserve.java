package com.project.pocketexpensemanager.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

public class SeeReserve extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor reserveCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.basic_list_fab, container, false);

        int[] adapterRowViews = new int[]{android.R.id.text1};
        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        reserveCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter reserveSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
                reserveCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        reserveSca.setDropDownViewResource(android.R.layout.simple_list_item_1);
        final ListView reserve_list = (ListView) view.findViewById(R.id.list);
        reserve_list.setAdapter(reserveSca);
        reserve_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                showEditReserveDialog(textView.getText().toString());
            }
        });
        mDb.close();

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateReserveDialog();
            }
        });


        return view;
    }

    private void showEditReserveDialog(final String current_reserve) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_reserve, null);
        dialogBuilder.setView(dialogView);
        ((EditText) dialogView.findViewById(R.id.reserve_text)).setText(current_reserve);
        dialogBuilder.setTitle("Edit Reserve");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String reserve = ((EditText) dialogView.findViewById(R.id.reserve_text)).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                reserveCursor = mDb.rawQuery("select * from " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_TYPE + " = ? ;", new String[]{reserve});
                if (reserveCursor != null && reserveCursor.getCount() == 0)
                    mDb.execSQL("update " + ReserveTable.TABLE_NAME + " set " +
                            ReserveTable.COLUMN_TYPE +
                            " = ? where " + ReserveTable.COLUMN_TYPE + " = ? ;", new String[]{reserve, current_reserve});

                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showCreateReserveDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_reserve, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Create Reserve");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String reserve = ((EditText) dialogView.findViewById(R.id.reserve_text)).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                reserveCursor = mDb.rawQuery("select * from " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_TYPE + " = ? ;", new String[]{reserve});
                if (reserveCursor != null && reserveCursor.getCount() == 0)
                    mDb.execSQL("insert into " + ReserveTable.TABLE_NAME + " (" +
                            ReserveTable.COLUMN_TYPE +
                            ") " + " values (?);", new String[]{reserve});
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
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
            dbHelper = DatabaseHelper.getInstance(getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCreatePostListener");
        }
    }

    @Override
    public void onDetach() {
        if (reserveCursor != null && !reserveCursor.isClosed())
            reserveCursor.close();
        super.onDetach();
    }
}
