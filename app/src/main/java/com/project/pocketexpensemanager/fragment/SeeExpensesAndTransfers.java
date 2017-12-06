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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.CategoryTable;
import com.project.pocketexpensemanager.database.table.ExpenseTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

public class SeeExpensesAndTransfers extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor transactionCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.see_transaction, container, false);
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();

        int[] adapterRowViews = new int[]{R.id.category, R.id.description, R.id.date, R.id.amount};
        String[] adapterColViews = new String[]{ExpenseTable.COLUMN_CATEGORY, ExpenseTable.COLUMN_DESCRIPTION, ExpenseTable.COLUMN_DATE, ExpenseTable.COLUMN_AMOUNT};
        transactionCursor = mDb.rawQuery("SELECT * FROM " + ExpenseTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter categorySca = new SimpleCursorAdapter(getActivity(), R.layout.transaction_item,
                transactionCursor, adapterColViews, adapterRowViews, 0);
        categorySca.setDropDownViewResource(R.layout.transaction_item);
        ((ListView) view.findViewById(R.id.transaction_list)).setAdapter(categorySca);

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
        if (transactionCursor != null && !transactionCursor.isClosed())
            transactionCursor.close();
        super.onDetach();
    }
}
