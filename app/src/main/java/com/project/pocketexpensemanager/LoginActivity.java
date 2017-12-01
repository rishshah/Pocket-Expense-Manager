package com.project.pocketexpensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.project.pocketexpensemanager.constants.Constants;

public class LoginActivity extends DriveBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setProgressBar((ProgressBar) findViewById(R.id.progress_bar));
        showProgress(false);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                showProgress(true);
            }
        });
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    @Override
    public void onDriveClientReady(){
        showMessage("Sign In Successful");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        showProgress(false);
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    showMessage( String.valueOf(resultCode) + " :Sign-in failed 1");
                    finish();
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    showMessage(String.valueOf(resultCode) + " :Sign-in failed 2");
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
