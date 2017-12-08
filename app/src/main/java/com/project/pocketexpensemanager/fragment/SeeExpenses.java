package com.project.pocketexpensemanager.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.table.ExpenseTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

public class SeeExpenses extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor expenseCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.see_expense, container, false);
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();

        int[] adapterRowViews = new int[]{R.id.category, R.id.description, R.id.date, R.id.hidden_id, R.id.amount};
        String[] adapterColViews = new String[]{ExpenseTable.COLUMN_CATEGORY, ExpenseTable.COLUMN_DESCRIPTION, ExpenseTable.COLUMN_DATE, "_id", "sum_amount"};
        expenseCursor = mDb.rawQuery("select expense._id, " + ExpenseTable.COLUMN_CATEGORY + ", " + ExpenseTable.COLUMN_DESCRIPTION + ", " + ExpenseTable.COLUMN_DATE +
                ", sum(" + ExpenseAmountTable.COLUMN_AMOUNT + ") as sum_amount " +
                "from " + ExpenseTable.TABLE_NAME + ", " + ExpenseAmountTable.TABLE_NAME + " where " +
                ExpenseTable.TABLE_NAME + "._id = " + ExpenseAmountTable.TABLE_NAME + "." + ExpenseAmountTable.COLUMN_EXPENSE_ID +
                " group by " + ExpenseTable.TABLE_NAME + "._id; ", null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.expense_item,
                expenseCursor, adapterColViews, adapterRowViews, 0);
        adapter.setDropDownViewResource(R.layout.expense_item);
        ListView expenseList = (ListView) view.findViewById(R.id.expense_list);
        expenseList.setAdapter(adapter);
        mDb.close();

        expenseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String expenseId = ((TextView) view.findViewById(R.id.hidden_id)).getText().toString();
                SQLiteDatabase mDb = dbHelper.getReadableDatabase();
                expenseCursor = mDb.rawQuery("select * from " + ExpenseTable.TABLE_NAME + " where _id = ? ;", new String[]{expenseId});
                if (expenseCursor.moveToFirst()) {
                    mDisplay.displayLinkedFragment(HomeActivity.VIEW_PARTICULAR_EXPENSE, expenseCursor, ((TextView) view.findViewById(R.id.amount)).getText().toString());
                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_create_expense);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.showContextMenu();
            }
        });

        registerForContextMenu(fab);

        return view;
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
        super.onDetach();
    }
}
