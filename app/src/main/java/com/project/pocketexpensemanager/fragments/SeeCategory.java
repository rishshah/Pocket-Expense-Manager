package com.project.pocketexpensemanager.fragments;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.utilities.Constants;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.tables.CategoryTable;
import com.project.pocketexpensemanager.database.tables.ExpenseTable;
import com.project.pocketexpensemanager.fragments.communication.Display;

public class SeeCategory extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor categoryCursor, expenseCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.basic_list_fab, container, false);
        int[] adapterRowViews = new int[]{android.R.id.text1};

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        categoryCursor = mDb.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + " where " + CategoryTable.COLUMN_ACTIVE + " = ?;", new String[]{String.valueOf(Constants.ACTIVATED)});
        SimpleCursorAdapter categorySca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
                categoryCursor, new String[]{CategoryTable.COLUMN_TYPE}, adapterRowViews, 0);
        categorySca.setDropDownViewResource(android.R.layout.simple_list_item_1);
        final ListView category_list = (ListView) view.findViewById(R.id.list);
        category_list.setAdapter(categorySca);
        category_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                showCreateCategoryDialog();
            }
        });

        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        return view;
    }

    private void showEditReserveDialog(final String current_category) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.one_edit_dialog, null);
        dialogBuilder.setView(dialogView);
        ((EditText) dialogView.findViewById(R.id.edit_text)).setText(current_category);
        dialogBuilder.setTitle("Edit Category");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String category = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                categoryCursor = mDb.rawQuery("select * from " + CategoryTable.TABLE_NAME + " where " + CategoryTable.COLUMN_TYPE + " = ? ;", new String[]{category});
                if (categoryCursor != null && categoryCursor.getCount() == 0)
                    mDb.execSQL("update " + CategoryTable.TABLE_NAME + " set " +
                            CategoryTable.COLUMN_TYPE +
                            " = ? where " + CategoryTable.COLUMN_TYPE + " = ? ;", new String[]{category, current_category});

                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_CATEGORY);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mDisplay.displayFragment(HomeActivity.SEE_CATEGORY);
            }
        });
        dialogBuilder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                expenseCursor = mDb.rawQuery("select * from " + ExpenseTable.TABLE_NAME + " where " + ExpenseTable.COLUMN_CATEGORY + " = ? ", new String[]{current_category});
                if (expenseCursor.getCount() > 0) {
                    mDb.execSQL("update " + CategoryTable.TABLE_NAME + " set " + CategoryTable.COLUMN_ACTIVE + " = ? " +
                            " where " + CategoryTable.COLUMN_TYPE + " = ? ;", new String[]{String.valueOf(Constants.DEACTIVATED), current_category});
                } else {
                    mDb.execSQL("delete from " + CategoryTable.TABLE_NAME +
                            " where " + CategoryTable.COLUMN_TYPE + " = ? ;", new String[]{current_category});
                }
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_CATEGORY);
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showCreateCategoryDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.one_edit_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Create Category");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String category = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                categoryCursor = mDb.rawQuery("select * from " + CategoryTable.TABLE_NAME + " where " + CategoryTable.COLUMN_TYPE + " = ? ;", new String[]{category});
                if (categoryCursor != null && categoryCursor.getCount() == 0)
                    mDb.execSQL("insert into " + CategoryTable.TABLE_NAME + " (" +
                            CategoryTable.COLUMN_TYPE + ", " +
                            CategoryTable.COLUMN_ACTIVE +
                            ") " + " values (?,?);", new String[]{category, String.valueOf(Constants.ACTIVATED)});
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_CATEGORY);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mDisplay.displayFragment(HomeActivity.SEE_CATEGORY);
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dbHelper = DatabaseHelper.getInstance(getActivity());
            mDisplay = (Display) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCreatePostListener");
        }
    }

    @Override
    public void onDetach() {
        if (categoryCursor != null && !categoryCursor.isClosed())
            categoryCursor.close();
        if (expenseCursor != null && !expenseCursor.isClosed())
            expenseCursor.close();

        super.onDetach();
    }
}
