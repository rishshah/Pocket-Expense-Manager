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
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

        logCursor = mDb.rawQuery("select * from " + LogTable.TABLE_NAME + ";", null);
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

        return view;
    }


    private void showEditTransferDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_transfer, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Edit Expense");

        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        int[] adapterRowViews = new int[]{android.R.id.text1};
        //Method Of Payment Picker
        mopCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter mopSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                mopCursor, new String[]{ReserveTable.COLUMN_TYPE}, adapterRowViews, 0);
        mopSca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) dialogView.findViewById(R.id.from_mode_spinner)).setAdapter(mopSca);
        ((Spinner) dialogView.findViewById(R.id.to_mode_spinner)).setAdapter(mopSca);
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
        ((TextView) dialogView.findViewById(R.id.date_text)).setText(transferCursor.getString(1));
        // Date Picker
        dialogView.findViewById(R.id.date_text).setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                            dialogView.findViewById(R.id.description_text).requestFocus();
                        }
                    }, mYear, mMonth, mDay);
                    mDatePicker.setTitle("Select date");
                    mDatePicker.show();
                }
            }
        });

        // Set current amount
        ((TextView) dialogView.findViewById(R.id.amount_text)).setText(transferCursor.getString(2));
        // Set current description
        ((TextView) dialogView.findViewById(R.id.description_text)).setText(transferCursor.getString(3));

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
            dialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener()

            {
                public void onClick (DialogInterface dialog,int whichButton){
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
