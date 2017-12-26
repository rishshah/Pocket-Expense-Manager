package com.project.pocketexpensemanager.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.utilities.Constants;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.tables.CategoryTable;
import com.project.pocketexpensemanager.database.tables.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.tables.ExpenseTable;
import com.project.pocketexpensemanager.database.tables.LogTable;
import com.project.pocketexpensemanager.fragments.communication.Display;

import java.text.ParseException;
import java.util.Calendar;

public class SeeExpense extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor expenseCursor, reserveCursor, categoryCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.see_expense, container, false);
        ((TextView) view.findViewById(R.id.amount)).setText(getArguments().getString("data"));
        ((TextView) view.findViewById(R.id.date)).setText(getArguments().getString(ExpenseTable.COLUMN_DATE));
        ((TextView) view.findViewById(R.id.category)).setText(getArguments().getString(ExpenseTable.COLUMN_CATEGORY));

        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        int[] adapterRowViews = new int[]{R.id.mop_caption, R.id.mop_amount};
        String[] adapterColViews = new String[]{ExpenseAmountTable.COLUMN_MOP, ExpenseAmountTable.COLUMN_AMOUNT};

        reserveCursor = mDb.rawQuery("select * from " + ExpenseAmountTable.TABLE_NAME + " where " +
                ExpenseAmountTable.COLUMN_EXPENSE_ID + " = ? ;", new String[]{getArguments().getString("_id")});
        SimpleCursorAdapter transactionSca = new SimpleCursorAdapter(getActivity(), R.layout.payment_detail_list_item,
                reserveCursor, adapterColViews, adapterRowViews, 0);
        transactionSca.setDropDownViewResource(R.layout.payment_detail_list_item);
        ListView expense_list = (ListView) view.findViewById(R.id.mop_list);
        expense_list.setAdapter(transactionSca);

        view.findViewById(R.id.fab_edit_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditExpenseDialog(view);
            }
        });

        view.findViewById(R.id.fab_save_expxense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExpense(view);
            }
        });

        view.findViewById(R.id.fab_delete_expense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteExpense();
            }
        });
        return view;
    }

    private void showEditExpenseDialog(final View view) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_expense_header, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Edit Expense");

        final View dateText = dialogView.findViewById(R.id.date_text);
        final View descriptionText = dialogView.findViewById(R.id.description_text);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        // Category picker
        categoryCursor = mDb.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter categorySca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                categoryCursor, new String[]{CategoryTable.COLUMN_TYPE}, new int[]{android.R.id.text1}, 0);
        categorySca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) dialogView.findViewById(R.id.category_spinner)).setAdapter(categorySca);
        // Set current category
        categoryCursor = mDb.rawQuery("SELECT _id FROM " + CategoryTable.TABLE_NAME + " where " + CategoryTable.COLUMN_TYPE + " = ?;", new String[]{((TextView) view.findViewById(R.id.category)).getText().toString()});
        if (categoryCursor.moveToFirst()) {
            ((Spinner) dialogView.findViewById(R.id.category_spinner)).setSelection(categoryCursor.getInt(0) - 1);
        }
        mDb.close();

        //Set current date
        ((EditText) dateText).setText(getArguments().getString(ExpenseTable.COLUMN_DATE));
        // Date Picker
        ((EditText) dateText).setInputType(InputType.TYPE_NULL);
        dialogView.findViewById(R.id.date_text).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String date = "";
                    try {
                        date = Constants.INPUT_FORMAT.format(Constants.OUTPUT_FORMAT.parse(((TextView) view.findViewById(R.id.date)).getText().toString()));
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
                            imm.hideSoftInputFromWindow(dateText.getWindowToken(), 0);
                            descriptionText.requestFocus();
                        }
                    }, mYear, mMonth, mDay);
                    mDatePicker.setTitle("Select date");
                    mDatePicker.show();
                    imm.hideSoftInputFromWindow(dateText.getWindowToken(), 0);
                    descriptionText.requestFocus();
                }
            }
        });

        // Set current description
        ((EditText) descriptionText).setText(((AppCompatActivity) getActivity()).getSupportActionBar().getTitle());

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newDescription = ((EditText) dialogView.findViewById(R.id.description_text)).getText().toString();
                String newDate = ((EditText) dialogView.findViewById(R.id.date_text)).getText().toString();
                String newCategory = ((TextView) ((Spinner) dialogView.findViewById(R.id.category_spinner)).getSelectedView()).getText().toString();

                ((TextView) view.findViewById(R.id.date)).setText(newDate);
                ((TextView) view.findViewById(R.id.category)).setText(newCategory);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(newDescription);
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void updateExpense(View view) {
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        expenseCursor = mDb.rawQuery("select * from " + ExpenseTable.TABLE_NAME + " where _id = ?", new String[]{getArguments().getString("_id")});
        if (expenseCursor != null && expenseCursor.getCount() == 1) {
            String newDescription = ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle().toString();
            String newDate = ((TextView) view.findViewById(R.id.date)).getText().toString();
            String newCategory = ((TextView) view.findViewById(R.id.category)).getText().toString();
            mDb.execSQL("update " + ExpenseTable.TABLE_NAME + " set " +
                            ExpenseTable.COLUMN_CATEGORY + " = ?, " +
                            ExpenseTable.COLUMN_DATE + " = ?, " +
                            ExpenseTable.COLUMN_DESCRIPTION + " = ? where _id = ?; ",
                    new String[]{newCategory, newDate, newDescription, getArguments().getString("_id")});

            mDb.execSQL("delete from " + ExpenseAmountTable.TABLE_NAME + "  where " + ExpenseAmountTable.COLUMN_EXPENSE_ID + " = ?; ",
                    new String[]{getArguments().getString("_id")});

            float amt = 0f;
            for (int i = 0; i < reserveCursor.getCount(); i++) {
                View child = ((ListView) view.findViewById(R.id.mop_list)).getChildAt(i);
                String reserve = ((TextView) child.findViewById(R.id.mop_caption)).getText().toString();
                String amount = ((EditText) child.findViewById(R.id.mop_amount)).getText().toString();
                if (amount.equals("")) {
                    amount = "0";
                }
                amt += Float.valueOf(amount);
                mDb.execSQL("insert into " + ExpenseAmountTable.TABLE_NAME + " (" +
                                ExpenseAmountTable.COLUMN_EXPENSE_ID + "," +
                                ExpenseAmountTable.COLUMN_MOP + "," +
                                ExpenseAmountTable.COLUMN_AMOUNT +
                                ") " + " values (?, ?, ?);",
                        new String[]{getArguments().getString("_id"), reserve, amount});
            }
            Calendar calendar = Calendar.getInstance();
            String currentDate = mDisplay.parseDate(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " : " +
                    String.valueOf(calendar.get(Calendar.MONTH) + 1) + " : " + String.valueOf(calendar.get(Calendar.YEAR)));
            mDb.execSQL("insert into " + LogTable.TABLE_NAME + " (" +
                            LogTable.COLUMN_TITLE + "," +
                            LogTable.COLUMN_DESCRIPTION_MAIN + "," +
                            LogTable.COLUMN_DESCRIPTION_SUB + "," +
                            LogTable.COLUMN_AMOUNT + "," +
                            LogTable.COLUMN_HIDDEN_ID + "," +
                            LogTable.COLUMN_LOG_DATE + "," +
                            LogTable.COLUMN_EVENT_DATE + "," +
                            LogTable.COLUMN_TYPE + ") " + " values (?, ?, ?, ?, ?, ?, ?, ?);",
                    new String[]{newCategory, newDescription, "Expense Updated", String.valueOf(amt), getArguments().getString("_id"), currentDate, newDate, ExpenseTable.TABLE_NAME});

        }
        mDb.close();
        mDisplay.displayFragment(HomeActivity.SEE_LOG);
    }

    private void deleteExpense() {
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        expenseCursor = mDb.rawQuery("select * from " + ExpenseTable.TABLE_NAME + " where _id = ?", new String[]{getArguments().getString("_id")});
        if (expenseCursor.moveToFirst()) {

            reserveCursor = mDb.rawQuery("select * from " + ExpenseAmountTable.TABLE_NAME + "  where " + ExpenseAmountTable.COLUMN_EXPENSE_ID + " = ?; ",
                    new String[]{getArguments().getString("_id")});

            float amt = 0f;
            while (reserveCursor.moveToNext()) {
                amt += reserveCursor.getFloat(2);

            }
            mDb.execSQL("delete from " + ExpenseAmountTable.TABLE_NAME + " where " + ExpenseAmountTable.COLUMN_EXPENSE_ID + " = ?", new String[]{getArguments().getString("_id")});
            mDb.execSQL("delete from " + ExpenseTable.TABLE_NAME + " where _id = ?", new String[]{getArguments().getString("_id")});

            Calendar calendar = Calendar.getInstance();
            String currentDate = mDisplay.parseDate(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " : " +
                    String.valueOf(calendar.get(Calendar.MONTH) + 1) + " : " + String.valueOf(calendar.get(Calendar.YEAR)));
            mDb.execSQL("insert into " + LogTable.TABLE_NAME + " (" +
                            LogTable.COLUMN_TITLE + "," +
                            LogTable.COLUMN_DESCRIPTION_MAIN + "," +
                            LogTable.COLUMN_DESCRIPTION_SUB + "," +
                            LogTable.COLUMN_AMOUNT + "," +
                            LogTable.COLUMN_HIDDEN_ID + "," +
                            LogTable.COLUMN_LOG_DATE + "," +
                            LogTable.COLUMN_EVENT_DATE + "," +
                            LogTable.COLUMN_TYPE + ") " + " values (?, ?, ?, ?, ?, ?, ?, ?);",
                    new String[]{expenseCursor.getString(2), expenseCursor.getString(3), "Expense Deleted", String.valueOf(amt), getArguments().getString("_id"), currentDate, expenseCursor.getString(1), ExpenseTable.TABLE_NAME});
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
            throw new ClassCastException(context.toString() + " must implement OnCreatePostListener");
        }
    }

    @Override
    public void onDetach() {
        if (expenseCursor != null && !expenseCursor.isClosed())
            expenseCursor.close();
        if (categoryCursor != null && !categoryCursor.isClosed())
            categoryCursor.close();
        if (reserveCursor != null && !reserveCursor.isClosed())
            reserveCursor.close();
        super.onDetach();
    }
}