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
import com.project.pocketexpensemanager.database.table.ExpenseTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

public class SeeExpenses extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor transactionCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.see_expense, container, false);
        SQLiteDatabase mDb = dbHelper.getWritableDatabase();

        int[] adapterRowViews = new int[]{R.id.category, R.id.description, R.id.date};
        String[] adapterColViews = new String[]{ExpenseTable.COLUMN_CATEGORY, ExpenseTable.COLUMN_DESCRIPTION, ExpenseTable.COLUMN_DATE};
        transactionCursor = mDb.rawQuery("SELECT * FROM " + ExpenseTable.TABLE_NAME + ";", null);
        SimpleCursorAdapter transactionSca = new SimpleCursorAdapter(getActivity(), R.layout.expense_item,
                transactionCursor, adapterColViews, adapterRowViews, 0);
        transactionSca.setDropDownViewResource(R.layout.expense_item);
        ListView transaction_list = (ListView) view.findViewById(R.id.expense_list);
        transaction_list.setAdapter(transactionSca);
//        transaction_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextView textView = (TextView) view.findViewById(android.R.id.text1);
//                LayoutInflater inflater = getActivity().getLayoutInflater();
//                final View editExpenseview = inflater.inflate(R.layout.create_expense, null);
//                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
//                transactionCursor = mDb.rawQuery("select * from " + ExpenseTable.TABLE_NAME + " where " +
//                        ExpenseTable.COLUMN_AMOUNT + " = ? and " +
//                        ExpenseTable.COLUMN_CATEGORY + " = ? and " +
//                        ExpenseTable.COLUMN_DATE + " = ? and " +
//                        ExpenseTable.COLUMN_DESCRIPTION + " = ?; ", new String[]{amount, category, date, description});
//                if (transactionCursor != null && transactionCursor.getCount() == 0)
//                    mDb.execSQL("update " + ExpenseTable.TABLE_NAME + " set (" +
//                            ExpenseTable.COLUMN_AMOUNT + ", " +
//                            ExpenseTable.COLUMN_CATEGORY + ", " +
//                            ExpenseTable.COLUMN_DATE + ", " +
//                            ExpenseTable.COLUMN_DESCRIPTION + ", " +
//                            ExpenseTable.COLUMN_MOP + ", " +
//                            "(?, ?, ?, ?, ?)  where " +
//                            ExpenseTable.COLUMN_AMOUNT + " = ? and " +
//                            ExpenseTable.COLUMN_CATEGORY + " = ? and " +
//                            ExpenseTable.COLUMN_DATE + " = ? and " +
//                            ExpenseTable.COLUMN_DESCRIPTION + " = ?; ", new String[]{amount, category, date, description});
//                ((EditText) editExpenseview.findViewById(R.id.expense_date_text)).setText("");
//                ((EditText) editExpenseview.findViewById(R.id.amount_text)).setText("");
//                ((EditText) editExpenseview.findViewById(R.id.description_text)).setText("");
//            }
//        });
        mDb.close();
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
