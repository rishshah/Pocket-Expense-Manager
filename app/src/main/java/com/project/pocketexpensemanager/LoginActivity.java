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
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String NEWUSER = "newUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String existingUsername = sharedPreferences.getString(USERNAME, "");
        if (existingUsername.equals("")) {

            findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String username = ((EditText) findViewById(R.id.username_text)).getText().toString();
                    String email = ((EditText) findViewById(R.id.email_text)).getText().toString();

                    if (username.equals("") || email.equals("")) {
                        Toast.makeText(getApplication(), "Invalid Username or Email", Toast.LENGTH_LONG).show();
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(USERNAME, username);
                        editor.putString(EMAIL, email);
                        editor.apply();
                        Toast.makeText(getApplication(), "Welcome ... ", Toast.LENGTH_LONG).show();
                        goToHomeActivity(300, true, username, email);
                    }
                }
            });
        } else {
            findViewById(R.id.username_text).setVisibility(View.GONE);
            findViewById(R.id.email_text).setVisibility(View.GONE);
            findViewById(R.id.continue_button).setVisibility(View.GONE);
            Toast.makeText(getApplication(), "Welcome Again... ", Toast.LENGTH_LONG).show();
            goToHomeActivity(500, false, sharedPreferences.getString(USERNAME, ""), sharedPreferences.getString(EMAIL, ""));
        }
    }

    private void goToHomeActivity(int time, final boolean newUser,final String username,final String email) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra(NEWUSER, newUser);
                intent.putExtra(USERNAME, username);
                intent.putExtra(EMAIL, email);
                startActivity(intent);
            }
        }, time);
    }
}
