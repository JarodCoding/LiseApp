package de.lisemeitnerschule.liseapp.Network.Security;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Pascal on 21.3.15.
 */
public class AuthenticationService extends Service{
    private static Authenticator sAuthenticator = null;

    public AuthenticationService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder res = null;
        if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT))
            res = getAuthenticator().getIBinder();
        return res;
    }

    private Authenticator getAuthenticator() {
        if (sAuthenticator == null)
            sAuthenticator = new Authenticator(this);
        return sAuthenticator;
    }
}
