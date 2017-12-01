package com.project.pocketexpensemanager;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveResourceClient;
import com.project.pocketexpensemanager.constant.Constants;

import java.util.HashSet;
import java.util.Set;

public abstract class DriveBase extends AppCompatActivity {
    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;
    /**
     * Handles ui progress
     */
    private ProgressBar mProgressBar;

    public void setProgressBar(ProgressBar p){
        mProgressBar = p;
    }

    public void showProgress(boolean visible){
        if(visible)
            mProgressBar.setVisibility(View.VISIBLE);
        else
            mProgressBar.setVisibility(View.GONE);
    }

    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    public void setDriveResourceClient(DriveResourceClient mDriveResourceClient) {
        this.mDriveResourceClient = mDriveResourceClient;
    }

    public void showMessage(String s) {
        Log.e("~~~~~~SM~~~~~~", s);
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public abstract void onDriveClientReady();

    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    public void signIn() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(mGoogleSignInClient.getSignInIntent(), Constants.REQUEST_CODE_SIGN_IN);
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    public void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveResourceClient =Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }



}
