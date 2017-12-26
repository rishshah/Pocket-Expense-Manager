package com.project.pocketexpensemanager.fragment;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.constant.Constants;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.database.table.ExpenseAmountTable;
import com.project.pocketexpensemanager.database.table.ReserveTable;
import com.project.pocketexpensemanager.database.table.TransferTable;
import com.project.pocketexpensemanager.fragment.communication.Display;

public class SeeReserve extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private Cursor reserveCursor, expenseCursor, transferCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.basic_list_fab, container, false);

        int[] adapterRowViews = new int[]{android.R.id.text1, android.R.id.text2};
        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        reserveCursor = mDb.rawQuery("with a(" + ReserveTable.COLUMN_TYPE + ",amt1) as (select " + ExpenseAmountTable.COLUMN_MOP + ", sum(" + ExpenseAmountTable.COLUMN_AMOUNT + ") from " + ExpenseAmountTable.TABLE_NAME + " group by " + ExpenseAmountTable.COLUMN_MOP + "), " +
                "b(" + ReserveTable.COLUMN_TYPE + ",amt2) as (select " + TransferTable.COLUMN_FROM_MODE + ", sum(" + TransferTable.COLUMN_AMOUNT + ")  from " + TransferTable.TABLE_NAME + " group by " + TransferTable.COLUMN_FROM_MODE + ")," +
                "c(" + ReserveTable.COLUMN_TYPE + ",amt3) as (select " + TransferTable.COLUMN_TO_MODE + ", sum(" + TransferTable.COLUMN_AMOUNT + ")  from " + TransferTable.TABLE_NAME + " group by " + TransferTable.COLUMN_TO_MODE + ")," +
                "final_one(_id, " + ReserveTable.COLUMN_TYPE + ", " + ReserveTable.COLUMN_ACTIVE + ", " + ReserveTable.COLUMN_START_AMT + ", amt1, amt2, amt3) as (select _id, " + ReserveTable.COLUMN_TYPE + ", " + ReserveTable.COLUMN_ACTIVE + ", CASE WHEN " + ReserveTable.COLUMN_START_AMT + " IS NULL THEN 0 ELSE " + ReserveTable.COLUMN_START_AMT + " END, CASE WHEN amt1 IS NULL THEN 0 ELSE amt1 END, CASE WHEN amt2 IS NULL THEN 0 ELSE amt2 END, CASE WHEN amt3 IS NULL THEN 0 ELSE amt3 END from (((reserve left natural outer join a) left natural outer join b) left natural outer join c))" +
                "select _id, " + ReserveTable.COLUMN_TYPE + ",  (" + ReserveTable.COLUMN_START_AMT + " + amt3 - amt2 - amt1) as balance from final_one where " + ReserveTable.COLUMN_ACTIVE + " = ?;", new String[]{String.valueOf(Constants.ACTIVATED)});
        SimpleCursorAdapter reserveSca = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2,
                reserveCursor, new String[]{ReserveTable.COLUMN_TYPE, "balance"}, adapterRowViews, 0);
        reserveSca.setDropDownViewResource(android.R.layout.simple_list_item_2);
        final ListView reserve_list = (ListView) view.findViewById(R.id.list);
        reserve_list.setAdapter(reserveSca);
        reserve_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showEditReserveDialog(((TextView) view.findViewById(android.R.id.text1)).getText().toString());
            }
        });
        mDb.close();

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateReserveDialog();
            }
        });

        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        return view;
    }

    private void showEditReserveDialog(final String current_reserve) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_reserve, null);
        dialogBuilder.setView(dialogView);
        ((EditText) dialogView.findViewById(R.id.reserve_text)).setText(current_reserve);

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        reserveCursor = mDb.rawQuery("select _id, " + ReserveTable.COLUMN_START_AMT + " from " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_TYPE + " = ? ", new String[]{current_reserve});
        if (reserveCursor.moveToFirst()) {
            ((EditText) dialogView.findViewById(R.id.reserve_amount)).setText(String.valueOf(reserveCursor.getFloat(1)));
        }
        dialogBuilder.setTitle("Edit Reserve");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String reserve = ((EditText) dialogView.findViewById(R.id.reserve_text)).getText().toString();
                String startAmount = ((EditText) dialogView.findViewById(R.id.reserve_amount)).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                reserveCursor = mDb.rawQuery("select * from " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_TYPE + " = ? ;", new String[]{reserve});
                Log.e("AMT", reserve + " ; " + startAmount);
                if (reserveCursor != null && (reserveCursor.getCount() == 0 || reserveCursor.moveToFirst() && reserveCursor.getString(1).equals(current_reserve))) {
                    mDb.execSQL("update " + ReserveTable.TABLE_NAME + " set " +
                                    ReserveTable.COLUMN_TYPE + " = ?, " +
                                    ReserveTable.COLUMN_START_AMT + " = ? where " + ReserveTable.COLUMN_TYPE + " = ? ;",
                            new String[]{reserve, startAmount, current_reserve});
                    Log.e("AMT", reserve + " ; " + startAmount);
                }
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        dialogBuilder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                expenseCursor = mDb.rawQuery("select * from " + ExpenseAmountTable.TABLE_NAME + " where " + ExpenseAmountTable.COLUMN_MOP + " = ? ", new String[]{current_reserve});
                transferCursor = mDb.rawQuery("select * from " + TransferTable.TABLE_NAME + " where " + TransferTable.COLUMN_FROM_MODE + " = ?  or " + TransferTable.COLUMN_TO_MODE + " = ?", new String[]{current_reserve, current_reserve});
                if (expenseCursor.getCount() > 0 || transferCursor.getCount() > 0) {
                    mDb.execSQL("update " + ReserveTable.TABLE_NAME + " set " + ReserveTable.COLUMN_ACTIVE + " = ? " +
                            " where " + ReserveTable.COLUMN_TYPE + " = ? ;", new String[]{String.valueOf(Constants.DEACTIVATED), current_reserve});
                } else {
                    mDb.execSQL("delete from " + ReserveTable.TABLE_NAME +
                            " where " + ReserveTable.COLUMN_TYPE + " = ? ;", new String[]{current_reserve});
                }
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void showCreateReserveDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_reserve, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Create Reserve");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String reserve = ((EditText) dialogView.findViewById(R.id.reserve_text)).getText().toString();
                String startAmount = ((EditText) dialogView.findViewById(R.id.reserve_amount)).getText().toString();
                SQLiteDatabase mDb = dbHelper.getWritableDatabase();
                reserveCursor = mDb.rawQuery("select * from " + ReserveTable.TABLE_NAME + " where " + ReserveTable.COLUMN_TYPE + " = ? ;", new String[]{reserve});
                if (reserveCursor != null && reserveCursor.getCount() == 0)
                    mDb.execSQL("insert into " + ReserveTable.TABLE_NAME + " (" +
                            ReserveTable.COLUMN_TYPE + ", " +
                            ReserveTable.COLUMN_START_AMT + ", " +
                            ReserveTable.COLUMN_ACTIVE +
                            ") " + " values (?, ?, ?);", new String[]{reserve, startAmount, String.valueOf(Constants.ACTIVATED)});
                mDb.close();
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mDisplay.displayFragment(HomeActivity.SEE_RESERVE);
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
        if (reserveCursor != null && !reserveCursor.isClosed())
            reserveCursor.close();
        if (expenseCursor != null && !expenseCursor.isClosed())
            expenseCursor.close();
        if (transferCursor != null && !transferCursor.isClosed())
            transferCursor.close();

        super.onDetach();
    }
}
