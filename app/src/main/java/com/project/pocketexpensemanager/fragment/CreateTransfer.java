package com.project.pocketexpensemanager.fragment;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.database.table.TransferTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.util.Calendar;

public class CreateTransfer extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor mopCursor;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.create_transfer, container, false);

        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        int[] adapterRowViews = new int[]{android.R.id.text1};
        //Method Of Payment Picker
        mopCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter mopSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                mopCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        mopSca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) view.findViewById(R.id.from_mode_spinner)).setAdapter(mopSca);
        ((Spinner) view.findViewById(R.id.to_mode_spinner)).setAdapter(mopSca);
        mDb.close();

        // Date Picker
        view.findViewById(R.id.transfer_date_text).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar currentDate = Calendar.getInstance();
                    int mYear = currentDate.get(Calendar.YEAR);
                    int mMonth = currentDate.get(Calendar.MONTH);
                    int mDay = currentDate.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                            String date =  String.valueOf(selectedday) + " : " + String.valueOf(selectedmonth + 1) + " : " + String.valueOf(selectedyear);
                            ((EditText) view.findViewById(R.id.transfer_date_text)).setText(mDisplay.parseDate(date));
                        }
                    }, mYear, mMonth, mDay);
                    mDatePicker.setTitle("Select date");
                    mDatePicker.show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_save_transfer);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = ((EditText) view.findViewById(R.id.transfer_date_text)).getText().toString();
                String amount = ((EditText) view.findViewById(R.id.amount_text)).getText().toString();
                String from_mode = ((Spinner) view.findViewById(R.id.from_mode_spinner)).getSelectedItem().toString();
                String to_mode = ((Spinner) view.findViewById(R.id.to_mode_spinner)).getSelectedItem().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                mDb.execSQL("insert into " + TransferTable.TABLE_NAME + " (" +
                                TransferTable.COLUMN_DATE + "," +
                                TransferTable.COLUMN_AMOUNT + "," +
                                TransferTable.COLUMN_FROM_MODE + "," +
                                TransferTable.COLUMN_TO_MODE +
                                ") " +" values (?, ?, ?, ?);",
                        new String[]{date, amount, from_mode, to_mode});
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
        if (mopCursor != null && !mopCursor.isClosed())
            mopCursor.close();
        super.onDetach();
    }


}
