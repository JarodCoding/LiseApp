package de.lisemeitnerschule.liseapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.reflect.Field;

import de.lisemeitnerschule.liseapp.Internal.InternalContract;
import de.lisemeitnerschule.liseapp.Internal.News.BaseFragment;
import de.lisemeitnerschule.liseapp.Internal.News.NewsFragment;
import de.lisemeitnerschule.liseapp.Network.Security.Authenticator;


public class MenuActivity extends ActionBarActivity
        implements NavigationDrawer.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawer mNavigationDrawer;


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
    private LinearLayout searchContainer;
    private EditText     toolbarSearchView;
    private ImageView    searchClearButton;
    private boolean      toolbarHomeButtonAnimating;

    protected void setupSearchbar() {
        searchContainer   = (LinearLayout) findViewById(R.id.search_container);
        toolbarSearchView = (EditText)  findViewById(R.id.search_view);
        searchClearButton = (ImageView) findViewById(R.id.search_clear);


        // Setup search container view
        //      try {
        // Set cursor colour to white
        // http://stackoverflow.com/a/26544231/1692770
        // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
        //          Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
        //          f.setAccessible(true);
        //          f.set(toolbarSearchView, R.drawable.edittext_whitecursor);
        //      } catch (Exception ignored) {
        //      }

        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                android.app.Fragment mainFragment = getFragmentManager().findFragmentById(R.id.container);
                if (mainFragment != null && mainFragment instanceof BaseFragment) {
                    ((BaseFragment) mainFragment).search(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Clear search text when clear button is tapped
        searchClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarSearchView.setText("");
            }
        });

        // Hide the search view
        searchContainer.setVisibility(View.GONE);



        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toolbarHomeButtonAnimating is a boolean that is initialized as false. It's used to stop the user pressing the home button while it is animating and breaking things.
                if (!toolbarHomeButtonAnimating) {

                    // Here you'll want to check if you have a search query set, if you don't then hide the search box.
                    // My main fragment handles this stuff, so I call its methods.
                    android.app.FragmentManager fragmentManager = getFragmentManager();
                    final android.app.Fragment fragment = fragmentManager.findFragmentById(R.id.container);
                    if (fragment != null && fragment instanceof BaseFragment) {
                        if (((BaseFragment) fragment).hasSearchQuery() || searchContainer.getVisibility() == View.VISIBLE) {
                            displaySearchView(false);

                            return;
                        }
                    }
                }

                if (mDrawerLayout.isDrawerOpen(findViewById(R.id.NavigationDrawer)))
                    mDrawerLayout.closeDrawer(findViewById(R.id.NavigationDrawer));
                else
                    mDrawerLayout.openDrawer(findViewById(R.id.NavigationDrawer));
            }
        });
    }
    private Menu menu;
    @Override
    public void onBackPressed() {
        android.app.FragmentManager fragmentManager = getFragmentManager();
        final android.app.Fragment mainFragment = fragmentManager.findFragmentById(R.id.container);
        if (mainFragment != null && mainFragment instanceof BaseFragment) {
            if (((BaseFragment) mainFragment).hasSearchQuery() || searchContainer.getVisibility() == View.VISIBLE) {
                displaySearchView(false);
                return;
            }
            }
        mNavigationDrawer.onBackPressed();
        super.onBackPressed();
    }
    public void displaySearchView(boolean visible) {

        if (visible) {
            // Stops user from being able to open drawer while searching
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            //hide all menu items
            for(int i = 0; i < menu.size(); i++){

                menu.getItem(i).setVisible(false);

            }

            searchContainer.setVisibility(View.VISIBLE);

            ((Toolbar)findViewById(R.id.main_toolbar)).setBackgroundColor(getResources().getColor(R.color.PrimaryWhite));
            Utilities.updateColor(R.color.background_material_light,this);
            // Animate the home icon to the back arrow
            toggleActionBarIcon(ActionDrawableState.ARROW, this.mNavigationDrawer.mDrawerToggle, true);
            // Shift focus to the search EditText
            toolbarSearchView.requestFocus();

            // Pop up the soft keyboard
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    toolbarSearchView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                    toolbarSearchView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                }
            }, 200);
        } else {
            // Allows user to open drawer again
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

            // Hide the EditText and restore the Toolbar-Menu.
            // This sometimes fails when it isn't postDelayed(), don't know why.
            toolbarSearchView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbarSearchView.setText("");
                    searchContainer.setVisibility(View.GONE);
                    ((Toolbar)findViewById(R.id.main_toolbar)).setBackgroundColor(getResources().getColor(R.color.PrimaryWhite));
                    Utilities.updateColor(R.color.background_material_light,MenuActivity.this);
                    for(int i = 0; i < menu.size(); i++){
                        menu.getItem(i).setVisible(true);
                    }
                }
            }, 200);

            // Turn the home button back into a drawer icon
            toggleActionBarIcon(ActionDrawableState.BURGER, this.mNavigationDrawer.mDrawerToggle, true);// Hide the keyboard because the search box has been hidden
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(toolbarSearchView.getWindowToken(), 0);
        }
    }
    private enum ActionDrawableState {
        BURGER, ARROW
    }

    /**
     * Modified version of this, http://stackoverflow.com/a/26836272/1692770<br>
     * I flipped the start offset around for the animations because it seemed like it was the wrong way around to me.<br>
     * I also added a listener to the animation so I can find out when the home button has finished rotating.
     */
    private void toggleActionBarIcon(final ActionDrawableState state, final ActionBarDrawerToggle toggle, boolean animate) {
        if (animate) {
            float start = state == ActionDrawableState.BURGER ? 1.0f : 0f;
            float end = Math.abs(start - 1);
                ValueAnimator offsetAnimator = ValueAnimator.ofFloat(start, end);
                offsetAnimator.setDuration(300);
                offsetAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float offset = (Float) animation.getAnimatedValue();
                        toggle.onDrawerSlide(null, offset);
                    }
                });
                offsetAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toolbarHomeButtonAnimating = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                toolbarHomeButtonAnimating = true;
                offsetAnimator.start();

        } else {
            if (state == ActionDrawableState.BURGER) {
                toggle.onDrawerClosed(null);
            } else {
                toggle.onDrawerOpened(null);
            }
        }
    }
    private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupAnimations();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawer = new NavigationDrawer(this,(ListView)findViewById(R.id.NavigationDrawerMainMenu),(ViewGroup)findViewById(R.id.NavigationDrawer),mDrawerLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }else{
            try {
                throw new Exception("Toolbar is null!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mTitle = getTitle();


        getSupportActionBar().setHomeButtonEnabled(true);

        // Set up the drawer.
        mNavigationDrawer.init(savedInstanceState);
        //setupSync();
        setupSearchbar();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        android.app.FragmentManager fragmentManager = getFragmentManager();
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
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawer.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will no it wont
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.search_action){
            displaySearchView(true);
            return true;
        }

        return mNavigationDrawer.onOptionsItemSelected(item)||super.onOptionsItemSelected(item);
    }
    public BaseFragment open(int sectionNumber) {
        BaseFragment fragment;
        switch (sectionNumber){
            case 1 : fragment = NewsFragment.newInstance(this);
                break;
            default: fragment = null;
        }

        return fragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mNavigationDrawer.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        mNavigationDrawer.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState, outPersistentState);
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        this.mNavigationDrawer.onPostCreate(savedInstanceState);
        super.onPostCreate(savedInstanceState);
    }
}
