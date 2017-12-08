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
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.LogTable;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.database.table.TransferTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.util.Calendar;

public class CreateTransfer extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor mopCursor;
    private Cursor transferCursor;

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
                            String date = String.valueOf(selectedday) + " : " + String.valueOf(selectedmonth + 1) + " : " + String.valueOf(selectedyear);
                            ((EditText) view.findViewById(R.id.date_text)).setText(mDisplay.parseDate(date));
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
            public void onClick(View v) {
                String description = ((EditText) view.findViewById(R.id.description_text)).getText().toString();
                String date = ((EditText) view.findViewById(R.id.date_text)).getText().toString();
                String amount = ((EditText) view.findViewById(R.id.amount_text)).getText().toString();
                String from_mode = ((TextView) ((Spinner) view.findViewById(R.id.from_mode_spinner)).getSelectedView()).getText().toString();
                String to_mode = ((TextView) ((Spinner) view.findViewById(R.id.to_mode_spinner)).getSelectedView()).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                mDb.execSQL("insert into " + TransferTable.TABLE_NAME + " (" +
                                TransferTable.COLUMN_DATE + "," +
                                TransferTable.COLUMN_AMOUNT + "," +
                                TransferTable.COLUMN_DESCRIPTION + "," +
                                TransferTable.COLUMN_FROM_MODE + "," +
                                TransferTable.COLUMN_TO_MODE +
                                ") " + " values (?, ?, ?, ?, ?);",
                        new String[]{date, amount, description, from_mode, to_mode});
                transferCursor = mDb.rawQuery("SELECT _id from " + TransferTable.TABLE_NAME + " order by _id DESC limit 1;", null);
                if (transferCursor.moveToFirst()) {
                    Calendar calendar = Calendar.getInstance();
                    String currentDate = mDisplay.parseDate(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " : " +
                            String.valueOf(calendar.get(Calendar.MONTH) + 1) + " : " + String.valueOf(calendar.get(Calendar.YEAR)));

                    String id = transferCursor.getString(0);
                    mDb.execSQL("insert into " + LogTable.TABLE_NAME + " (" +
                                    LogTable.COLUMN_TITLE + "," +
                                    LogTable.COLUMN_DESCRIPTION_MAIN + "," +
                                    LogTable.COLUMN_DESCRIPTION_SUB + "," +
                                    LogTable.COLUMN_AMOUNT + "," +
                                    LogTable.COLUMN_HIDDEN_ID + "," +
                                    LogTable.COLUMN_LOG_DATE + "," +
                                    LogTable.COLUMN_EVENT_DATE + "," +
                                    LogTable.COLUMN_TYPE + ") " + " values (?, ?, ?, ?, ?, ?, ?, ?);",
                            new String[]{from_mode + " -> " + to_mode, description, "Transfer Created", amount, id, currentDate, date, TransferTable.TABLE_NAME});
                }
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_LOG);
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
