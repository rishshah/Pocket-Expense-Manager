package com.project.pocketexpensemanager.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.fragment.communication.Display;

public class SeeExpensesAndTransfers extends Fragment {

    private Display mDisplay;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.see_expense, container, false);

        // TODO Show list of expenses and transfer
//        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
//        SQLiteDatabase mDb = dbHelper.getWritableDatabase();
//        int[] adapterRowViews=new int[]{android.R.id.text1};
//        Cursor categoryCursor = mDb.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + ";", null);
//        SimpleCursorAdapter categorySca=new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
//                categoryCursor, new String[]{CategoryTable.COLUMN_TYPE}, adapterRowViews,0);
//        categorySca.setDropDownViewResource(android.R.layout.simple_list_item_1);
//        ((ListView) view.findViewById(R.id.expense_list)).setAdapter(categorySca);
//        categoryCursor.close();

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
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCreatePostListener");
        }
    }

}
