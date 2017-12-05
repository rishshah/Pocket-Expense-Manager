package com.project.pocketexpensemanager.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.CategoryTable;
import com.project.pocketexpensemanager.database.table.ExpenseTable;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.util.Calendar;

public class CreateExpense extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor categoryCursor, mopCursor;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.create_expense, container, false);

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        int[] adapterRowViews = new int[]{android.R.id.text1};
        // Category picker
        categoryCursor = mDb.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter categorySca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                categoryCursor, new String[]{CategoryTable.COLUMN_TYPE}, adapterRowViews, 0);
        categorySca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) view.findViewById(R.id.category_spinner)).setAdapter(categorySca);

        //Method Of Payment Picker
        mopCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter mopSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                mopCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        mopSca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) view.findViewById(R.id.mop_spinner)).setAdapter(mopSca);
        mDb.close();
        // Date Picker
        view.findViewById(R.id.expense_date_text).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar currentDate = Calendar.getInstance();
                    int mYear = currentDate.get(Calendar.YEAR);
                    int mMonth = currentDate.get(Calendar.MONTH);
                    int mDay = currentDate.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                            ((EditText) view.findViewById(R.id.expense_date_text)).setText(String.valueOf(selectedday) + " : " + String.valueOf(selectedmonth + 1) + " : " + String.valueOf(selectedyear));
                        }
                    }, mYear, mMonth, mDay);
                    mDatePicker.setTitle("Select date");
                    mDatePicker.show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_save_expxense);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = ((EditText) view.findViewById(R.id.expense_date_text)).getText().toString();
                String category = ((Spinner) view.findViewById(R.id.category_spinner)).getSelectedItem().toString();
                String description = ((EditText) view.findViewById(R.id.description_text)).getText().toString();
                String amount = ((EditText) view.findViewById(R.id.amount_text)).getText().toString();
                String mop = ((Spinner) view.findViewById(R.id.mop_spinner)).getSelectedItem().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                mDb.execSQL("insert into " + ExpenseTable.TABLE_NAME + " (" +
                                ExpenseTable.COLUMN_DATE + "," +
                                ExpenseTable.COLUMN_CATEGORY + "," +
                                ExpenseTable.COLUMN_DESCRIPTION + "," +
                                ExpenseTable.COLUMN_AMOUNT + "," +
                                ExpenseTable.COLUMN_MOP +
                                ") " +" values (?, ?, ?, ?, ?);",
                        new String[]{date, category, description, amount, mop});
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_TRANSACTIONS);
            }
        });

        return view;
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
        if (categoryCursor != null && !categoryCursor.isClosed())
            categoryCursor.close();
        if (mopCursor != null && !mopCursor.isClosed())
            mopCursor.close();
        super.onDetach();
    }

}
