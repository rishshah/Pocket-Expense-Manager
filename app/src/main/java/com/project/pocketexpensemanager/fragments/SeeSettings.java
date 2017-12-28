package com.project.pocketexpensemanager.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.pocketexpensemanager.HomeActivity;
import com.project.pocketexpensemanager.LoginActivity;
import com.project.pocketexpensemanager.R;
import com.project.pocketexpensemanager.database.DatabaseHelper;
import com.project.pocketexpensemanager.fragments.communication.Display;

public class SeeSettings extends Fragment {

    private Display mDisplay;
    private DatabaseHelper dbHelper;
    private static final String BACKUP = "Backup Settings";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings, container, false);
        View usernameView = view.findViewById(R.id.field_account);
        View emailView = view.findViewById(R.id.field_email);
        View backupView = view.findViewById(R.id.field_backup);

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AccountDetails", Context.MODE_PRIVATE);
        fillViewDetails(usernameView, LoginActivity.USERNAME, sharedPreferences.getString(LoginActivity.USERNAME, ""));
        fillViewDetails(emailView, LoginActivity.EMAIL, sharedPreferences.getString(LoginActivity.EMAIL, ""));
        fillViewDetails(backupView, BACKUP, "");

        usernameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialogBox(LoginActivity.USERNAME);
            }
        });
        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialogBox(LoginActivity.EMAIL);
            }
        });

        backupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.showContextMenu();
            }
        });
        registerForContextMenu(backupView);

        return view;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.backup, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_backup:
                dbHelper.exportToDrive();
                return true;
            case R.id.item_import:
                dbHelper.importFromDrive();
                return true;
            default:
                return false;
        }
    }

    private void fillViewDetails(View view, String title, String value) {
        ((TextView) view.findViewById(R.id.field_caption)).setText(title);
        ((TextView) view.findViewById(R.id.field_value)).setText(value);

        int img = 0;
        switch (title) {
            case LoginActivity.USERNAME:
                img = getActivity().getResources().getIdentifier("com.project.pocketexpensemanager:drawable/ic_account", null, null);
                break;
            case LoginActivity.EMAIL:
                img = getActivity().getResources().getIdentifier("com.project.pocketexpensemanager:drawable/ic_email", null, null);
                break;
            case BACKUP:
                img = getActivity().getResources().getIdentifier("com.project.pocketexpensemanager:drawable/ic_backup", null, null);
                break;
        }
        ((ImageView) view.findViewById(R.id.icon)).setImageResource(img);
    }


    private void showEditDialogBox(final String type) {
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AccountDetails", Context.MODE_PRIVATE);
        String existing = sharedPreferences.getString(type, "");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.one_edit_dialog, null);
        dialogBuilder.setView(dialogView);

        ((EditText) dialogView.findViewById(R.id.edit_text)).setHint("Enter " + type);
        ((EditText) dialogView.findViewById(R.id.edit_text)).setText(existing);
        dialogBuilder.setTitle("Edit " + type);

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String edited = ((EditText) dialogView.findViewById(R.id.edit_text)).getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(type, edited);
                editor.apply();
                mDisplay.displayFragment(HomeActivity.SEE_SETTINGS);
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
}
