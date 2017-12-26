package com.project.pocketexpensemanager.fragments;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.utilities.Constants;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.tables.LogTable;
import com.project.pocketexpensemanager.database.tables.ReserveTable;
import com.project.pocketexpensemanager.database.tables.TransferTable;
import com.project.pocketexpensemanager.fragments.communication.Display;

import java.util.Calendar;

public class CreateTransfer extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor mopCursor;
    private Cursor transferCursor;
    boolean firstTime = true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.create_transfer, container, false);
        final View dateText = view.findViewById(R.id.date_text);
        final View amountText = view.findViewById(R.id.amount_text);
        final View descriptionText = view.findViewById(R.id.description_text);
        final View fromModeSpinner = view.findViewById(R.id.from_mode_spinner);
        final View toModeSpinner = view.findViewById(R.id.to_mode_spinner);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();

        //DescriptionText
        descriptionText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                if(keyCode == 66 && keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return false;
            }
        });

        //Method Of Payment Picker
        int[] adapterRowViews = new int[]{android.R.id.text1};
        mopCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_ACTIVE + " = ? ;", new String[]{String.valueOf(Constants.ACTIVATED)});
        SimpleCursorAdapter mopSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                mopCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        mopSca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) fromModeSpinner).setAdapter(mopSca);
        ((Spinner) fromModeSpinner).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!firstTime){
                    toModeSpinner.performClick();
                } else {
                    firstTime = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        ((Spinner) view.findViewById(R.id.to_mode_spinner)).setAdapter(mopSca);
        mDb.close();

        // Date Picker
        ((EditText)dateText).setInputType(InputType.TYPE_NULL);
        dateText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                    amountText.requestFocus();
                    imm.showSoftInput(amountText,InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        //AmountText
        amountText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                if(keyCode == 66 && keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    fromModeSpinner.performClick();
                }
                return false;
            }
        });

        //SaveFab
        view.findViewById(R.id.fab_save_transfer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransfer(view);
            }
        });

        descriptionText.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        return view;
    }

    private void saveTransfer(View view){
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
                    new String[]{from_mode + " -> " + to_mode, description, "Transfer Created", amount, id, currentDate, date,
                            TransferTable.TABLE_NAME});
        }
        mDb.close();
        mDisplay.displayFragment(HomeActivity.SEE_LOG);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDisplay = (Display) context;
            dbHelper = DatabaseHelper.getInstance(getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public void onDetach() {
        if (mopCursor != null && !mopCursor.isClosed())
            mopCursor.close();
        if (transferCursor != null && !transferCursor.isClosed())
            transferCursor.close();
        super.onDetach();
    }


}
