package com.ghostflying.portalwaitinglist;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;

import java.io.IOException;

/**
 * The Activity to auth when user open this app first time.
 */
public class AuthActivity extends Activity {

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    // The scope used, read all mails
    static final String SCOPE =
            "oauth2:https://www.googleapis.com/auth/gmail.readonly";
    String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ((SignInButton)findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToken();
            }
        });
    }

    private void getToken(){
        if (mEmail == null)
            pickUserAccount();
        else
            new GetTokenTask(this).execute();
    }

    /**
     * Show the dialog for user to select one Google Account
     */
    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    /**
     * Process different return status of different activities.
     * @param requestCode {@inheritDoc}
     * @param resultCode {@inheritDoc   }
     * @param data {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                new GetTokenTask(this).execute();
                // With the account name acquired, go get the auth token
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, getString(R.string.alert_no_choose_account), Toast.LENGTH_SHORT).show();
            }
        }else if ((requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK){
            getToken();
        }
    }

    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            AuthActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    /**
     * The main logic to get the token.
     */
    private class GetTokenTask extends AsyncTask<Void, Void, Void> {
        Activity mActivity;
        String token;

        public GetTokenTask(Activity activity){
            this.mActivity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                token = fetchToken();
            }
            catch (IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                R.string.network_error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            if (token != null){
                // return to main activity
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                // set flag to remove self.
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // store the auth account
                SettingUtil.setAccount(mEmail);
                startActivity(mainIntent);
            }
        }

        protected String fetchToken() throws IOException{
            try{
                return GoogleAuthUtil.getToken(mActivity, mEmail, SCOPE);
            }
            catch (UserRecoverableAuthException e){
                handleException(e);
            }
            catch (GoogleAuthException e){
                e.printStackTrace();
            }
            return null;
        }
    }

}
