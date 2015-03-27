package de.lisemeitnerschule.liseapp.Network.Security;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import de.lisemeitnerschule.liseapp.Network.Session;

/**
 * Created by Pascal on 21.3.15.
 */
public class Authenticator extends AbstractAccountAuthenticator {
    public Context context;
    public Authenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(context, Login.class);
        intent.putExtra(Login.PARAM_ACCOUNT_CREATE, true);
        intent.putExtra(Login.PARAM_ACCOUNT_TYPE, accountType);
        intent.putExtra(Login.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.+

        final AccountManager am = AccountManager.get(context);

        String authToken = am.getPassword(account);

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            try{
                System.err.println("starting test for authtoken: "+authToken);
                new Session(account.name,authToken);
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                System.err.println("sucsess");
            return result;
            }catch (Exception e){
                e.printStackTrace();
                //authentication failed invalidating AuthToken
                am.invalidateAuthToken(account.type,authToken);

            }
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(context, Login.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(Login.PARAM_ACCOUNT_CREATE, false);
        intent.putExtra(Login.PARAM_ACCOUNT_TYPE, account.type);
        intent.putExtra(Login.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(Login.PARAM_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return "LISEAPPAUTHTOKEN";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }
}
