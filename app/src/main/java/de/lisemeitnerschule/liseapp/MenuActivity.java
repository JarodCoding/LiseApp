package de.lisemeitnerschule.liseapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.transition.ChangeBounds;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import de.lisemeitnerschule.liseapp.Internal.InternalContract;
import de.lisemeitnerschule.liseapp.Internal.News.NewsFragment;
import de.lisemeitnerschule.liseapp.Network.Security.Authenticator;


public class MenuActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setupAnimations(){
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementEnterTransition(new ChangeBounds());
    }
    public static void syncManually(Context context){
        Account[] accounts = AccountManager.get(context).getAccountsByType(Authenticator.accountType);

        for(Account current:accounts){
             try{
                 ContentResolver.requestSync(current, InternalContract.AUTHORITY, new Bundle());
            }catch (Exception e){
                 e.printStackTrace();
            }
        }
    }
    public static void syncManually(Context context,Account account){
        try{
            ContentResolver.requestSync(account, InternalContract.AUTHORITY, new Bundle());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setupSync(){
        Account[] accounts;
        ContentProviderClient InterContentProviderClient = getContentResolver().acquireContentProviderClient(InternalContract.CONTENT_URI);

        if((accounts = AccountManager.get(this).getAccountsByType(Authenticator.accountType)).length == 0) {
           //create a new Account if none exists
           startActivity(Authenticator.addAccount(this));
        }
        for(Account current:accounts){
            ContentResolver.setMasterSyncAutomatically(true);
            ContentResolver.setIsSyncable(current, InternalContract.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(current, InternalContract.AUTHORITY, true);
            try {
                if(InterContentProviderClient.query(InternalContract.News.CONTENT_URI,new String[]{InternalContract.News._ID},"",null,InternalContract.News.SORT_ORDER_DEFAULT).getCount()==0){
                    try{
                        ContentResolver.requestSync(current, InternalContract.AUTHORITY, new Bundle());
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                try{
                    ContentResolver.requestSync(current, InternalContract.AUTHORITY, new Bundle());
                }catch (Exception e1){
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupAnimations();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        setupSync();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, open(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public Fragment open(int sectionNumber) {
        Fragment fragment;
        switch (sectionNumber){
            case 1 : fragment = NewsFragment.newInstance(this);
                break;
            default: fragment = PlaceholderFragment.newInstance(sectionNumber);
        }

        return fragment;
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment  fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_menu, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MenuActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    }
