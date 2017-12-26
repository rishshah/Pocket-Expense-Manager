package com.project.pocketexpensemanager.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.constant.Constants;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.ExpenseTable;
import com.project.pocketexpensemanager.database.table.LogTable;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.database.table.TransferTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.text.ParseException;
import java.util.Calendar;

public class SeeLog extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor expenseCursor, logCursor, transferCursor, mopCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.basic_list_fab, container, false);
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();

        int[] adapterRowViews = new int[]{
                R.id.title,
                R.id.description_main,
                R.id.description_sub,
                R.id.event_date,
                R.id.log_date,
                R.id.amount,
                R.id.hidden_type,
                R.id.hidden_id};

        String[] adapterColViews = new String[]{
                LogTable.COLUMN_TITLE,
                LogTable.COLUMN_DESCRIPTION_MAIN,
                LogTable.COLUMN_DESCRIPTION_SUB,
                LogTable.COLUMN_EVENT_DATE,
                LogTable.COLUMN_LOG_DATE,
                LogTable.COLUMN_AMOUNT,
                LogTable.COLUMN_TYPE,
                LogTable.COLUMN_HIDDEN_ID};

        logCursor = mDb.rawQuery("select * from " + LogTable.TABLE_NAME + " order by _id desc;", null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.log_item,
                logCursor, adapterColViews, adapterRowViews, 0);
        adapter.setDropDownViewResource(R.layout.log_item);
        ListView logList = (ListView) view.findViewById(R.id.list);
        logList.setAdapter(adapter);
        mDb.close();

        logList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = ((TextView) view.findViewById(R.id.hidden_id)).getText().toString();
                String type = ((TextView) view.findViewById(R.id.hidden_type)).getText().toString();

                SQLiteDatabase mDb = dbHelper.getReadableDatabase();
                if (type.equals(ExpenseTable.TABLE_NAME)) {
                    expenseCursor = mDb.rawQuery("select * from " + ExpenseTable.TABLE_NAME + " where _id = ? ;", new String[]{id});
                    if (expenseCursor.moveToFirst()) {
                        mDisplay.displayLinkedFragment(HomeActivity.VIEW_PARTICULAR_EXPENSE, expenseCursor, ((TextView) view.findViewById(R.id.amount)).getText().toString());
                    }
                    mDb.close();
                } else if (type.equals(TransferTable.TABLE_NAME)) {
                    transferCursor = mDb.rawQuery("select * from " + TransferTable.TABLE_NAME + " where _id = ? ;", new String[]{id});
                    if (transferCursor.moveToFirst()) {
                        showEditTransferDialog();
                    }
                    mDb.close();
                }

            }
        });


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.showContextMenu();
            }
        });
        registerForContextMenu(fab);

        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        return view;
    }


    private void showEditTransferDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_transfer, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Edit Transfer");

        final View dateText = dialogView.findViewById(R.id.date_text);
        final View amountText = dialogView.findViewById(R.id.amount_text);
        final View fromModeSpinner = dialogView.findViewById(R.id.from_mode_spinner);
        final View toModeSpinner = dialogView.findViewById(R.id.to_mode_spinner);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();

        //Method Of Payment Picker
        int[] adapterRowViews = new int[]{android.R.id.text1};
        mopCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter mopSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                mopCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        mopSca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) fromModeSpinner).setAdapter(mopSca);
        ((Spinner) toModeSpinner).setAdapter(mopSca);
        // Set current category
        mopCursor = mDb.rawQuery("SELECT _id FROM " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_TYPE + " = ?;", new String[]{transferCursor.getString(4)});
        if (mopCursor.moveToFirst()) {
            ((Spinner) dialogView.findViewById(R.id.from_mode_spinner)).setSelection(mopCursor.getInt(0) - 1);
        }
        // Set current category
        mopCursor = mDb.rawQuery("SELECT _id FROM " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_TYPE + " = ?;", new String[]{transferCursor.getString(5)});
        if (mopCursor.moveToFirst()) {
            ((Spinner) dialogView.findViewById(R.id.to_mode_spinner)).setSelection(mopCursor.getInt(0) - 1);
        }
        mDb.close();

        // Set current date
        ((EditText) dateText).setText(transferCursor.getString(1));
        // Date Picker
        ((EditText) dateText).setInputType(InputType.TYPE_NULL);
        dateText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String date = "";
                    try {
                        date = Constants.INPUT_FORMAT.format(Constants.OUTPUT_FORMAT.parse(transferCursor.getString(1)));
                    } catch (ParseException e) {
                        Log.e("DATE ERR..", e.getMessage());
                    }
                    String[] dateParts = date.split(" : ");
                    int mYear = Integer.valueOf(dateParts[2]);
                    int mMonth = Integer.valueOf(dateParts[1]) - 1;
                    int mDay = Integer.valueOf(dateParts[0]);

                    DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                            String date = String.valueOf(selectedday) + " : " + String.valueOf(selectedmonth + 1) + " : " + String.valueOf(selectedyear);
                            ((EditText) dialogView.findViewById(R.id.date_text)).setText(mDisplay.parseDate(date));
                            amountText.requestFocus();
                        }
                    }, mYear, mMonth, mDay);
                    mDatePicker.setTitle("Select date");
                    mDatePicker.show();
                    amountText.requestFocus();
                    imm.hideSoftInputFromWindow(dateText.getWindowToken(), 0);
                }
            }
        });

        // Set current amount
        ((EditText) amountText).setText(transferCursor.getString(2));
        //AmountText
        amountText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                if (keyCode == 66 && keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return false;
            }
        });
        // Set current description
        ((EditText) dialogView.findViewById(R.id.description_text)).setText(transferCursor.getString(3));

        dialogView.findViewById(R.id.fab_save_transfer).setVisibility(View.GONE);

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newDescription = ((EditText) dialogView.findViewById(R.id.description_text)).getText().toString();
                String newAmount = ((EditText) dialogView.findViewById(R.id.amount_text)).getText().toString();
                String newDate = ((EditText) dialogView.findViewById(R.id.date_text)).getText().toString();
                String newFromMode = ((TextView) ((Spinner) dialogView.findViewById(R.id.from_mode_spinner)).getSelectedView()).getText().toString();
                String newToMode = ((TextView) ((Spinner) dialogView.findViewById(R.id.to_mode_spinner)).getSelectedView()).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                mDb.execSQL("update " + TransferTable.TABLE_NAME + " set " +
                                TransferTable.COLUMN_DATE + " = ?," +
                                TransferTable.COLUMN_AMOUNT + " = ?," +
                                TransferTable.COLUMN_DESCRIPTION + " = ?," +
                                TransferTable.COLUMN_FROM_MODE + " = ?," +
                                TransferTable.COLUMN_TO_MODE + " = ? where _id = ?",
                        new String[]{newDate, newAmount, newDescription, newFromMode, newToMode, transferCursor.getString(0)});

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
                        new String[]{newFromMode + " -> " + newToMode, newDescription, "Transfer Updated", newAmount, id, currentDate, newDate, TransferTable.TABLE_NAME});

                mDisplay.displayFragment(HomeActivity.SEE_LOG);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                mDb.execSQL("delete from " + TransferTable.TABLE_NAME +
                        " where _id = ? ;", new String[]{transferCursor.getString(0)});

                Calendar calendar = Calendar.getInstance();
                String currentDate = mDisplay.parseDate(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " : " +
                        String.valueOf(calendar.get(Calendar.MONTH) + 1) + " : " + String.valueOf(calendar.get(Calendar.YEAR)));

                //Update Log
                mDb.execSQL("insert into " + LogTable.TABLE_NAME + " (" +
                                LogTable.COLUMN_TITLE + "," +
                                LogTable.COLUMN_DESCRIPTION_MAIN + "," +
                                LogTable.COLUMN_DESCRIPTION_SUB + "," +
                                LogTable.COLUMN_AMOUNT + "," +
                                LogTable.COLUMN_HIDDEN_ID + "," +
                                LogTable.COLUMN_LOG_DATE + "," +
                                LogTable.COLUMN_EVENT_DATE + "," +
                                LogTable.COLUMN_TYPE + ") " + " values (?, ?, ?, ?, ?, ?, ?, ?);",
                        new String[]{transferCursor.getString(4) + " -> " + transferCursor.getString(5), transferCursor.getString(3),
                                "Transfer Deleted", transferCursor.getString(2), transferCursor.getString(0), currentDate,
                                transferCursor.getString(1), TransferTable.TABLE_NAME});
                mDb.close();
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.create_transaction, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_expense:
                mDisplay.displayFragment(HomeActivity.CREATE_EXPENSE);
                return true;
            case R.id.item_transfer:
                mDisplay.displayFragment(HomeActivity.CREATE_TRANSFER);
                return true;
            default:
                return false;
        }
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
        if (expenseCursor != null && !expenseCursor.isClosed())
            expenseCursor.close();
        if (logCursor != null && !logCursor.isClosed())
            logCursor.close();
        if (transferCursor != null && !transferCursor.isClosed())
            transferCursor.close();
        if (mopCursor != null && !mopCursor.isClosed())
            mopCursor.close();

        super.onDetach();
    }
}
