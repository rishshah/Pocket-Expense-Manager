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
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.util.Calendar;

public class CreateExpense extends Fragment {
    private Display mDisplay;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.create_expense, container, false);
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();

        int[] adapterRowViews = new int[]{android.R.id.text1};
        // Category picker
        Cursor categoryCursor = mDb.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter categorySca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                categoryCursor, new String[]{CategoryTable.COLUMN_TYPE}, adapterRowViews, 0);
        categorySca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) view.findViewById(R.id.category_spinner)).setAdapter(categorySca);
        categoryCursor.close();

        //Method Of Payment Picker
        Cursor mopCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter mopSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                mopCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        mopSca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) view.findViewById(R.id.mop_spinner)).setAdapter(mopSca);
        mopCursor.close();

        // Date Picker
        view.findViewById(R.id.date_text).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar currentDate = Calendar.getInstance();
                    int mYear = currentDate.get(Calendar.YEAR);
                    int mMonth = currentDate.get(Calendar.MONTH);
                    int mDay = currentDate.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                            ((EditText) view.findViewById(R.id.date_text)).setText(String.valueOf(selectedday) + " : " + String.valueOf(selectedmonth + 1) + ":" + String.valueOf(selectedyear));
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
            public void onClick(View view) {
                // TODO Save Expense in db and display other fragment
                mDisplay.displayFragment(HomeActivity.SEE_EXPENSES);
            }
        });

        return view;
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
