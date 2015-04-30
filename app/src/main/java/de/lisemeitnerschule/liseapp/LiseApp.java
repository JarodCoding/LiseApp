package de.lisemeitnerschule.liseapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;

import de.lisemeitnerschule.liseapp.Internal.InternalContract;
import de.lisemeitnerschule.liseapp.Network.Security.Authenticator;
import de.lisemeitnerschule.liseapp.Network.Session;

/**
 * Created by Pascal on 21.3.15.
 */
public class LiseApp {
    public static final String URL = "http://liseapptesting.ddns.net:1610";
    public static final void introduction(BaseActivity activity){
        //Setup Accounts

            AccountManager manager = AccountManager.get(activity);
            Account[] accounts = manager.getAccountsByType(Authenticator.accountType);
                if(accounts.length==0){

                    //Create public Account
                        Account account = new Account(activity.getString(R.string.publicUser), Authenticator.accountType) ;
                        manager.addAccountExplicitly(account, Session.publicHash, null);
                        manager.setPassword(account,Session.publicHash);
                        ContentResolver.setMasterSyncAutomatically(true);
                        ContentResolver.setSyncAutomatically(account, InternalContract.AUTHORITY,true);

                    //Ask user to login
                        Intent LoginIntent = Authenticator.addAccount(activity, activity.getString(R.string.skip), null);
                        activity.startActivity(LoginIntent);

                }else if(accounts.length==1){
                    if(manager.getPassword(accounts[0]).equals(Session.publicHash)){//Public user exists: Ask user to login
                        Intent LoginIntent = Authenticator.addAccount(activity, activity.getString(R.string.skip), null);
                        activity.startActivity(LoginIntent);

                    }else{//User account exists: Create public Account
                        Account account = new Account(activity.getString(R.string.publicUser), Authenticator.accountType) ;
                        manager.addAccountExplicitly(account, Session.publicHash, null);
                        manager.setPassword(account,Session.publicHash);
                        ContentResolver.setSyncAutomatically(account, InternalContract.AUTHORITY,true);

                    }
            }


    }
    public static void login(Activity activity){
        Intent LoginIntent = Authenticator.addAccount(activity);
        activity.startActivity(LoginIntent);
    }

}