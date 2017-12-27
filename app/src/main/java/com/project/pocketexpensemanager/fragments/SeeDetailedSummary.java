package com.project.pocketexpensemanager.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.tables.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.tables.ExpenseTable;
import com.project.pocketexpensemanager.fragments.communication.Display;
import com.project.pocketexpensemanager.utilities.Constants;

public class SeeDetailedSummary extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor expenseCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.summary_detailed, container, false);

        // Set Title
        String currentMonth = getArguments().getString("month");
        String currentYear = getArguments().getString("year");
        ((TextView) view.findViewById(R.id.title)).setText(currentMonth + " " + currentYear);


        //SummaryTable
        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        int[] adapterRowViews = new int[]{R.id.date_text, R.id.category_text, R.id.amount_text, R.id.description_text};
        String[] adapterColViews = new String[]{ExpenseTable.COLUMN_DATE, ExpenseTable.COLUMN_CATEGORY, "amount", ExpenseTable.COLUMN_DESCRIPTION};

        expenseCursor = mDb.rawQuery("select " + ExpenseTable.TABLE_NAME + "._id, " + ExpenseTable.COLUMN_DATE + ", " + ExpenseTable.COLUMN_DESCRIPTION + ", " + ExpenseTable.COLUMN_CATEGORY + ", sum(" + ExpenseAmountTable.COLUMN_AMOUNT + ") as amount from " +
                        ExpenseTable.TABLE_NAME + ", " + ExpenseAmountTable.TABLE_NAME + " where " +
                        ExpenseAmountTable.COLUMN_EXPENSE_ID + " = " + ExpenseTable.TABLE_NAME + "._id  and " +
                        ExpenseTable.COLUMN_DATE + " like ? group by " + ExpenseTable.TABLE_NAME + "._id order by " + ExpenseTable.COLUMN_DATE + " desc;",
                new String[]{currentMonth.substring(0, 3) + "%" + currentYear});

        SimpleCursorAdapter transactionSca = new SimpleCursorAdapter(getActivity(), R.layout.summary_item,
                expenseCursor, adapterColViews, adapterRowViews, 0);
        transactionSca.setDropDownViewResource(R.layout.summary_item);
        ListView expense_list = (ListView) view.findViewById(R.id.mop_list);
        expense_list.setAdapter(transactionSca);

        float amt = 0f;
        while (expenseCursor.moveToNext()) {
            amt += expenseCursor.getFloat(4);
        }
        ((TextView) view.findViewById(R.id.expense_total_text)).setText(String.valueOf(amt));
        //EditMonthYearDialog
        view.findViewById(R.id.fab_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_time_selection_dialog(view);
            }
        });

        return view;
    }

    private void show_time_selection_dialog(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.choose_month_year, null);
        dialogBuilder.setView(dialogView);

        String selected_time = ((TextView) view.findViewById(R.id.title)).getText().toString();
        String selectedMonth = selected_time.split(" ")[0];
        String selectedYear = selected_time.split(" ")[1];

        //Assign Months
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Constants.MONTHS);
        ((Spinner) dialogView.findViewById(R.id.month_spinner)).setAdapter(adapter);

        int currentMonthIndex = -1;
        for (int i = 0; i < Constants.MONTHS.length; i++) {
            if (Constants.MONTHS[i].equals(selectedMonth)) {
                currentMonthIndex = i;
                break;
            }
        }
        // Set Default Month
        if (currentMonthIndex != -1) {
            ((Spinner) dialogView.findViewById(R.id.month_spinner)).setSelection(currentMonthIndex);
        }

        // Set Default Year
        ((EditText) dialogView.findViewById(R.id.year_text)).setText(selectedYear);

        dialogBuilder.setTitle("Select Month and Year");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newYear = ((EditText) dialogView.findViewById(R.id.year_text)).getText().toString();
                String newMonth = ((TextView) ((Spinner) dialogView.findViewById(R.id.month_spinner)).getSelectedView()).getText().toString();
                mDisplay.displayLinkedFragment(HomeActivity.SEE_DETAILED_SUMMARY, null, new String[]{newMonth, newYear});
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
        super.onDetach();
    }
}