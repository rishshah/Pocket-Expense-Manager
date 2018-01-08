package com.project.pocketexpensemanager.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.tables.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.tables.ExpenseTable;
import com.project.pocketexpensemanager.fragments.communication.Display;
import com.project.pocketexpensemanager.utilities.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeeCategorywiseSummary extends Fragment {
    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor expenseCursor;
    private PieChart pieChart;
    private Map<String, Float> categoryMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.summary_categorywise, container, false);

        // Set Title
        String currentMonth = getArguments().getString("month");
        String currentYear = getArguments().getString("year");
        ((TextView) view.findViewById(R.id.title)).setText(currentMonth + " " + currentYear);

        //ExpenseTotal
        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        String query = "select " + ExpenseTable.TABLE_NAME + "._id, " + ExpenseTable.COLUMN_DATE + ", " + ExpenseTable.COLUMN_DESCRIPTION + ", " + ExpenseTable.COLUMN_CATEGORY + ", sum(" + ExpenseAmountTable.COLUMN_AMOUNT + ") as amount from " +
                ExpenseTable.TABLE_NAME + ", " + ExpenseAmountTable.TABLE_NAME + " where " +
                ExpenseAmountTable.COLUMN_EXPENSE_ID + " = " + ExpenseTable.TABLE_NAME + "._id  and " +
                ExpenseTable.COLUMN_DATE + " like ? group by " + ExpenseTable.TABLE_NAME + "._id order by " + ExpenseTable.COLUMN_DATE + " desc;";
        Log.e("TAG", "onCreateView: " + query );
        expenseCursor = mDb.rawQuery(query,
                new String[]{currentMonth.substring(0, 3) + "%" + currentYear});

        float amt = 0f;
        categoryMap = new HashMap<>();
        while (expenseCursor.moveToNext()) {
            amt += expenseCursor.getFloat(4);
            Log.e("TAG", "inForLoop: " + expenseCursor.getFloat(4));

            if (!categoryMap.containsKey(expenseCursor.getString(3))) {
                categoryMap.put(expenseCursor.getString(3), expenseCursor.getFloat(4));
            } else {
                categoryMap.put(expenseCursor.getString(3), categoryMap.get(expenseCursor.getString(3)) + expenseCursor.getFloat(4));
            }
        }
        ((TextView) view.findViewById(R.id.expense_total_text)).setText(String.valueOf(amt));
        mDb.close();

        //EditMonthYearDialog
        view.findViewById(R.id.fab_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_time_selection_dialog(view);
            }
        });

        //Piechart
        pieChart = (PieChart) view.findViewById(R.id.pie_chart);
        modelProperties();
        addData();
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.e("event", "onValueSelected: " + e.toString());
            }

            @Override
            public void onNothingSelected() {

            }
        });
        return view;
    }

    private void addData() {
        List<PieEntry> yEntries = new ArrayList<>();

        for (String category : categoryMap.keySet()) {
            yEntries.add(new PieEntry(categoryMap.get(category), category));
        }

        PieDataSet pieDataSet = new PieDataSet(yEntries, "");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(new int[]{
                R.color.color3,
                R.color.color2,
                R.color.color1,
                R.color.color8,
                R.color.color6,
                R.color.color5,
                R.color.color7,
                R.color.color4,
                R.color.color9
        }, getActivity());

        pieChart.setData(new PieData(pieDataSet));
        pieChart.invalidate();
    }

    private void modelProperties() {

        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleAlpha(150);
        pieChart.setCenterText("Categorywise Expenditure");
        pieChart.setCenterTextSize(20);
        pieChart.setDrawEntryLabels(true);
        //Legend
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10f);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
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
        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newYear = ((EditText) dialogView.findViewById(R.id.year_text)).getText().toString();
                String newMonth = ((TextView) ((Spinner) dialogView.findViewById(R.id.month_spinner)).getSelectedView()).getText().toString();
                try {
                    if (Integer.valueOf(newYear) == 0) {
                        HomeActivity.showMessage(getActivity(), "Enter valid year");
                        return;
                    }
                } catch (NumberFormatException e) {
                    HomeActivity.showMessage(getActivity(), "Enter valid year");
                    return;
                }
                mDisplay.displayLinkedFragment(HomeActivity.SEE_CATEGORYWISE_SUMMARY, null, new String[]{newMonth, newYear});
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
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public void onDetach() {
        if (expenseCursor != null && !expenseCursor.isClosed())
            expenseCursor.close();
        super.onDetach();
    }
}