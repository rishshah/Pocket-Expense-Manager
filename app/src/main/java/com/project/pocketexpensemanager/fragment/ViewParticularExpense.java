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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.project.pocketexpensemanager.database.table.CategoryTable;
import com.project.pocketexpensemanager.database.table.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.table.ExpenseTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

import java.text.ParseException;

public class ViewParticularExpense extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor expenseCursor, reserveCursor, categoryCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.view_particular_expense, container, false);
        ((TextView) view.findViewById(R.id.amount)).setText(getArguments().getString("data"));
        ((TextView) view.findViewById(R.id.date)).setText(getArguments().getString(ExpenseTable.COLUMN_DATE));
        ((TextView) view.findViewById(R.id.category)).setText(getArguments().getString(ExpenseTable.COLUMN_CATEGORY));

        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
        int[] adapterRowViews = new int[]{R.id.mop_caption, R.id.mop_amount};
        String[] adapterColViews = new String[]{ExpenseAmountTable.COLUMN_MOP, ExpenseAmountTable.COLUMN_AMOUNT};

        reserveCursor = mDb.rawQuery("select * from " + ExpenseAmountTable.TABLE_NAME + " where " +
                ExpenseAmountTable.COLUMN_EXPENSE_ID + " = ? ;", new String[]{getArguments().getString("_id")});
        SimpleCursorAdapter transactionSca = new SimpleCursorAdapter(getActivity(), R.layout.payment_detail_edit,
                reserveCursor, adapterColViews, adapterRowViews, 0);
        transactionSca.setDropDownViewResource(R.layout.payment_detail_edit);
        ListView expense_list = (ListView) view.findViewById(R.id.mop_list);
        expense_list.setAdapter(transactionSca);

        view.findViewById(R.id.fab_edit_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditReserveDialog(view);
            }
        });

        view.findViewById(R.id.fab_save_expxense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExpense(view);
            }
        });
        return view;
    }

    private void showEditReserveDialog(final View view) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_expense_header, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Edit Expense");

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        // Category picker
        categoryCursor = mDb.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter categorySca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item,
                categoryCursor, new String[]{CategoryTable.COLUMN_TYPE}, new int[]{android.R.id.text1}, 0);
        categorySca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) dialogView.findViewById(R.id.category_spinner)).setAdapter(categorySca);
        // Set current category
        categoryCursor = mDb.rawQuery("SELECT _id FROM " + CategoryTable.TABLE_NAME + " where " + CategoryTable.COLUMN_TYPE + " = ?;", new String[]{getArguments().getString(ExpenseTable.COLUMN_CATEGORY)});
        if (categoryCursor.moveToFirst()) {
            ((Spinner) dialogView.findViewById(R.id.category_spinner)).setSelection(categoryCursor.getInt(0)-1);
        }
        mDb.close();

        //Set current date
        ((TextView) dialogView.findViewById(R.id.expense_date_text)).setText(getArguments().getString(ExpenseTable.COLUMN_DATE));
        // Date Picker
        dialogView.findViewById(R.id.expense_date_text).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    String date = "";
                    try {
                         date = Constants.INPUT_FORMAT.format(Constants.OUTPUT_FORMAT.parse(getArguments().getString(ExpenseTable.COLUMN_DATE)));
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
                            ((EditText) dialogView.findViewById(R.id.expense_date_text)).setText(mDisplay.parseDate(date));
                            dialogView.findViewById(R.id.description_text).requestFocus();
                        }
                    }, mYear, mMonth, mDay);
                    mDatePicker.setTitle("Select date");
                    mDatePicker.show();
                }
            }
        });

        // Set current description
        ((TextView) dialogView.findViewById(R.id.description_text)).setText(getArguments().getString(ExpenseTable.COLUMN_DESCRIPTION));

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newDescription = ((EditText) dialogView.findViewById(R.id.description_text)).getText().toString();
                String newDate = ((EditText) dialogView.findViewById(R.id.expense_date_text)).getText().toString();
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

            for (int i = 0; i < reserveCursor.getCount(); i++) {
                View child = ((ListView) view.findViewById(R.id.mop_list)).getChildAt(i);
                String reserve = ((TextView) child.findViewById(R.id.mop_caption)).getText().toString();
                String amount = ((EditText) child.findViewById(R.id.mop_amount)).getText().toString();
                if (amount.equals("")) {
                    amount = "0";
                }
                mDb.execSQL("insert into " + ExpenseAmountTable.TABLE_NAME + " (" +
                                ExpenseAmountTable.COLUMN_EXPENSE_ID + "," +
                                ExpenseAmountTable.COLUMN_MOP + "," +
                                ExpenseAmountTable.COLUMN_AMOUNT +
                                ") " + " values (?, ?, ?);",
                        new String[]{getArguments().getString("_id"), reserve, amount});
            }
        }
        mDisplay.displayFragment(HomeActivity.SEE_EXPENSES);
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