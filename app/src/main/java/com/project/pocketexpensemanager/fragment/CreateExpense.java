package com.project.pocketexpensemanager.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.CategoryTable;
import com.project.pocketexpensemanager.database.table.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.table.ExpenseTable;
import com.project.pocketexpensemanager.database.table.LogTable;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.util.Calendar;

public class CreateExpense extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor categoryCursor, mopCursor, expenseCursor;


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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_save_expxense);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = ((EditText) view.findViewById(R.id.date_text)).getText().toString();
                String category = ((TextView) ((Spinner) view.findViewById(R.id.category_spinner)).getSelectedView()).getText().toString();
                String description = ((EditText) view.findViewById(R.id.description_text)) .getText().toString();

                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                mDb.execSQL("insert into " + ExpenseTable.TABLE_NAME + " (" +
                                ExpenseTable.COLUMN_DATE + "," +
                                ExpenseTable.COLUMN_CATEGORY + "," +
                                ExpenseTable.COLUMN_DESCRIPTION +
                                ") " + " values (?, ?, ?);",
                        new String[]{date, category, description});

                expenseCursor = mDb.rawQuery("SELECT _id from " + ExpenseTable.TABLE_NAME + " order by _id DESC limit 1;", null);
                if (expenseCursor.moveToFirst()) {
                    String[] mop = ((TextView) view.findViewById(R.id.amount_text)).getText().toString().split(", ");
                    String id = expenseCursor.getString(0);
                    float amt = 0f;
                    for (String payment : mop) {
                        String reserve = payment.split("-")[0];
                        String amount = payment.split("-")[1];
                        amt += Float.valueOf(amount);
                        mDb.execSQL("insert into " + ExpenseAmountTable.TABLE_NAME + " (" +
                                        ExpenseAmountTable.COLUMN_EXPENSE_ID + "," +
                                        ExpenseAmountTable.COLUMN_MOP + "," +
                                        ExpenseAmountTable.COLUMN_AMOUNT +
                                        ") " + " values (?, ?, ?);",
                                new String[]{id, reserve, amount});
                    }

                    Calendar calendar = Calendar.getInstance();
                    String currentDate = mDisplay.parseDate(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " : " +
                            String.valueOf(calendar.get(Calendar.MONTH) + 1) + " : " + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                    mDb.execSQL("insert into " + LogTable.TABLE_NAME + " (" +
                                    LogTable.COLUMN_TITLE + "," +
                                    LogTable.COLUMN_DESCRIPTION_MAIN + "," +
                                    LogTable.COLUMN_DESCRIPTION_SUB + "," +
                                    LogTable.COLUMN_AMOUNT + "," +
                                    LogTable.COLUMN_HIDDEN_ID + "," +
                                    LogTable.COLUMN_LOG_DATE + "," +
                                    LogTable.COLUMN_EVENT_DATE + "," +
                                    LogTable.COLUMN_TYPE + ") " + " values (?, ?, ?, ?, ?, ?, ?, ?);",
                            new String[]{category, description, "Expense Created", String.valueOf(amt), id, currentDate, date, ExpenseTable.TABLE_NAME});
                }

                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_LOG);
            }
        });

        view.findViewById(R.id.add_payment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDetailedPayment(view);
            }
        });
        return view;
    }

    private void addDetailedPayment(final View view) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.payment_detail_dialog, null);
        builderSingle.setView(dialogView);
        builderSingle.setTitle("Amount and Method Of Payment:-");

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        mopCursor = mDb.rawQuery("SELECT * FROM " + ReserveTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.payment_detail_list_item,
                mopCursor, new String[]{ReserveTable.COLUMN_TYPE}, new int[]{R.id.mop_caption}, 0);
        adapter.setDropDownViewResource(R.layout.payment_detail_list_item);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(dialogView.findViewById(R.id.amount_text), InputMethodManager.SHOW_IMPLICIT);
        final ListView paymentList = (ListView) dialogView.findViewById(R.id.payment_list);
        paymentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                v.findViewById(R.id.mop_amount).requestFocus();
                Log.e("ERR", v.toString());
            }
        });
        paymentList.setAdapter(adapter);

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e("CANCEL", String.valueOf(which));
                dialog.dismiss();
            }
        });
        builderSingle.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.e("SAVE", String.valueOf(whichButton));
                String finalString = "";
                for (int i = 0; i < mopCursor.getCount(); i++) {
                    View child = paymentList.getChildAt(i);
                    String reserve = ((TextView) child.findViewById(R.id.mop_caption)).getText().toString();
                    String amount = ((EditText) child.findViewById(R.id.mop_amount)).getText().toString();
                    if (amount.equals("")) {
                        amount = "0";
                    }
                    finalString += reserve + "-" + amount + ", ";
                }
                finalString = finalString.substring(0, finalString.length() - 2);
                ((TextView) view.findViewById(R.id.amount_text)).setText(finalString);
                dialog.dismiss();
            }
        });
        AlertDialog b = builderSingle.create();
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
        if (categoryCursor != null && !categoryCursor.isClosed())
            categoryCursor.close();
        if (expenseCursor != null && !expenseCursor.isClosed())
            expenseCursor.close();
        if (mopCursor != null && !mopCursor.isClosed())
            mopCursor.close();
        super.onDetach();
    }

}
