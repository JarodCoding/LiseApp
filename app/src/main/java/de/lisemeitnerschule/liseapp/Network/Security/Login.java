package de.lisemeitnerschule.liseapp.Network.Security;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.lisemeitnerschule.liseapp.Network.Session;
import de.lisemeitnerschule.liseapp.R;


/**
 * A login screen that offers login via email/password.
 */
public class Login extends ActionBarActivity {
    public static final String PARAM_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public static final String PARAM_ACCOUNT_CREATE = "ACCOUNT_CREATE";
    public static final String PARAM_ACCOUNT_NAME = "ACCOUNT_NAME";
    public static final String PARAM_AUTHTOKEN_TYPE = "AUTHTOKEN_TYPE";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView  ;
    private EditText mPasswordView  ;
    private View     mProgressView  ;
    private View     mLoginFormView ;
    private String   AccountType    ;
    private boolean  CreateNew      ;
    private String   AuthTokenType  ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sync_onCreate(savedInstanceState);

        setContentView(R.layout.login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        AccountType = getIntent().getStringExtra(PARAM_ACCOUNT_TYPE);
        CreateNew = getIntent().getBooleanExtra(PARAM_ACCOUNT_CREATE,true);
            if(!CreateNew){
                mUsernameView.setText(getIntent().getStringExtra(PARAM_ACCOUNT_NAME));
                setTitle(R.string.modify_title);
            }
        AuthTokenType = getIntent().getStringExtra(PARAM_AUTHTOKEN_TYPE);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if(username == null||password == null){
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();

        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password,this);
            mAuthTask.execute((Void) null);
        }
    }



    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
    }

    // Android Sync Stuff
                private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
                private Bundle mResultBundle = null;
                public void finish() {
                    if (mAccountAuthenticatorResponse != null) {
                        // send the result bundle back if set, otherwise send an error.
                        if (mResultBundle != null) {
                            mAccountAuthenticatorResponse.onResult(mResultBundle);
                        } else {
                            mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                                    "canceled");
                        }
                        mAccountAuthenticatorResponse = null;
                    }
                    super.finish();
                }
                protected void sync_onCreate(Bundle icicle) {

                    mAccountAuthenticatorResponse =
                            getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

                    if (mAccountAuthenticatorResponse != null) {
                        mAccountAuthenticatorResponse.onRequestContinued();
                    }
                }
                public final void setAccountAuthenticatorResult(Bundle result) {
                    mResultBundle = result;
                }






    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Intent> {

        private final String mUsername;
        private final String mPassword;
        private final Login parent;

        UserLoginTask(String username, String password,Login parent) {
            mUsername = username;
            mPassword = password;
            this.parent = parent;
        }

        @Override
        protected Intent doInBackground(Void... params) {
            String SecretHash = null;
            try {

                SecretHash = Session.login(mUsername,mPassword);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            final Intent res = new Intent();
            res.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
            res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountType);
            res.putExtra(AccountManager.KEY_AUTHTOKEN, SecretHash);
            return res;

        }

        @Override
        protected void onPostExecute(final Intent res) {
            mAuthTask = null;
            showProgress(false);

            if (res!=null) {
                AccountManager manager = AccountManager.get(getApplicationContext());

                //account Info
                    String accountName       = res.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)                           ;
                    String authtoken         = res.getStringExtra(AccountManager.KEY_AUTHTOKEN)                              ;
                    final Account account    = new Account(accountName, res.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)) ;

                if(CreateNew){
                    manager.addAccountExplicitly(account, authtoken, null);
                }else{
                    manager.setPassword(account,authtoken);
                }
                setAccountAuthenticatorResult(res.getExtras());
                setResult(RESULT_OK, res);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



