package com.project.pocketexpensemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    public static final String USERNAME = "Username";
    public static final String EMAIL = "EmailId";
    public static final String NEWUSER = "newUser";
    public static final String DRIVE_ID = "DRIVE_ID";
    public static final String  SHARED_PREF_NAME = "AccountDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String existingUsername = sharedPreferences.getString(USERNAME, "");
        if (existingUsername.equals("")) {

            findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String username = ((EditText) findViewById(R.id.username_text)).getText().toString();
                    String email = ((EditText) findViewById(R.id.email_text)).getText().toString();

                    if (username.equals("") || email.equals("")) {
                        HomeActivity.showMessage(getApplication(), "Invalid Username or Email");
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(USERNAME, username);
                        editor.putString(EMAIL, email);
                        editor.apply();
                        Toast.makeText(getApplication(), "Welcome " + username + "... ", Toast.LENGTH_SHORT).show();
                        goToHomeActivity(300, true);
                    }
                }
            });
        } else {
            findViewById(R.id.username_text).setVisibility(View.GONE);
            findViewById(R.id.email_text).setVisibility(View.GONE);
            findViewById(R.id.continue_button).setVisibility(View.GONE);
            Toast.makeText(getApplication(), "Welcome Again... ", Toast.LENGTH_SHORT).show();
            goToHomeActivity(500, false);
        }
    }

    private void goToHomeActivity(int time, final boolean newUser) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra(NEWUSER, newUser);
                startActivity(intent);
            }
        }, time);
    }
}
