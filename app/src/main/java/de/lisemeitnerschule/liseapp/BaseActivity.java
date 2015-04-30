package de.lisemeitnerschule.liseapp;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import de.lisemeitnerschule.liseapp.Internal.News.NewsFragment;
import de.lisemeitnerschule.liseapp.Network.Security.Authenticator;


public class BaseActivity extends ActionBarActivity
        implements NavigationDrawerCallbacks {

    private ImageView backarrow;




    private Search search;
    //Searchbar
        protected void setupSearchbar() {
        searchContainer   = (LinearLayout) findViewById(R.id.search_container);
        toolbarSearchView = (EditText)  findViewById(R.id.search_view);
        searchClearButton = (ImageView) findViewById(R.id.search_clear);
        backarrow         = (ImageView) findViewById(R.id.backarrow);


        // Setup search container view
              try {
        // Set cursor colour to black
        // http://stackoverflow.com/a/26544231/1692770
        // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
                  Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                  f.setAccessible(true);
                  f.set(toolbarSearchView, R.drawable.cursor);
              } catch (Exception ignored) {
              }
            DrawerArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            DrawerArrow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                android.app.Fragment mainFragment = getFragmentManager().findFragmentById(R.id.container);
                if (search!=null) {
                    search.search(s.toString());
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
                // Shift focus to the search EditText
                toolbarSearchView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(toolbarSearchView, InputMethodManager.SHOW_FORCED);

                toolbarSearchView.setText("");
            }
        });
            backarrow.setColorFilter(Color.BLACK);
            backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySearchView(false);
            }
        });
        // Hide the search view
        searchContainer.setVisibility(View.GONE);


            View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toolbarHomeButtonAnimating is a boolean that is initialized as false. It's used to stop the user pressing the home button while it is animating and breaking things.
                if (!toolbarHomeButtonAnimating) {

                        if (search != null) {
                            displaySearchView(false);
                            return;
                        }
                }

                if (mNavigationDrawerFragment.isDrawerOpen())
                    mNavigationDrawerFragment.closeDrawer();
                else
                    mNavigationDrawerFragment.openDrawer();
            }
        };

            Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
            toolbar.setNavigationOnClickListener(onClickListener);


    }
    private Drawable DrawerArrow;

    public void displaySearchView(boolean visible) {
        final Menu menu = mToolbar.getMenu();

        if (visible) {
            // Stops user from being able to open drawer while searching
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            //hide all menu items
            for(int i = 0; i < menu.size(); i++){

                menu.getItem(i).setVisible(false);

            }


            // Animate the home icon to the back arrow and when done display the search view (so we can actually see the animation
            toggleActionBarIcon(ActionDrawableState.ARROW, this.mNavigationDrawerFragment.getActionBarDrawerToggle(), true);

        } else {
            // Allows user to open drawer again
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


            // Hide the EditText and restore the Toolbar-Menu.
            // This sometimes fails when it isn't postDelayed(), don't know why.
            toolbarSearchView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbarSearchView.setText("");
                    searchContainer.setVisibility(View.GONE);
                    ((Toolbar) findViewById(R.id.main_toolbar)).setBackgroundColor(getResources().getColor(R.color.myPrimaryColor));
                    mNavigationDrawerFragment.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                    //getSupportActionBar().setHomeButtonEnabled(true);
                    Utilities.removeStaticStatusBarColor( BaseActivity.this);
                    new ColorDrawable();
                    for (int i = 0; i < menu.size(); i++) {
                        menu.getItem(i).setVisible(true);
                    }
                    // Turn the home button back into a drawer icon
                    toggleActionBarIcon(ActionDrawableState.BURGER,mNavigationDrawerFragment.getActionBarDrawerToggle(), true);// Hide the keyboard because the search box has been hidden
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(toolbarSearchView.getWindowToken(), 0);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }, 200);


        }
        search = ((BaseFragment)getFragmentManager().findFragmentById(R.id.container)).displaySearch(visible);
    }
    //ActionBarControl
        private enum ActionDrawableState {
            BURGER, ARROW
        }
        /**
         * Modified version of this, http://stackoverflow.com/a/26836272/1692770<br>
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
                    if(state == ActionDrawableState.ARROW){
                        searchContainer.setVisibility(View.VISIBLE);

                        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);

                        toolbar.setBackgroundColor(Color.WHITE);
                        mNavigationDrawerFragment.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);

                        getSupportActionBar().setDisplayShowHomeEnabled(false);
                        //getSupportActionBar().setHomeButtonEnabled(false);

                        Utilities.setStaticStatusBarColor(R.color.background_material_light,BaseActivity.this);
                        // Shift focus to the search EditText
                        toolbarSearchView.requestFocus();

                        // Pop up the soft keyboard
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                toolbarSearchView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                                toolbarSearchView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                            }
                        }, 200);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }

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

    private LinearLayout searchContainer;
    private EditText toolbarSearchView;
    private ImageView searchClearButton;
    private boolean      toolbarHomeButtonAnimating;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    public static final String MainPrefs = "LiseGeneralPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.removeStaticStatusBarColor(this);
        setContentView(R.layout.activity_main_menu);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, mDrawerLayout, mToolbar);
        // populate the navigation drawer
         mNavigationDrawerFragment.setUserData(AccountManager.get(this).getAccountsByType(Authenticator.accountType));
        setupSearchbar();

        SharedPreferences settings = getSharedPreferences(MainPrefs, 0);

        if (settings.getBoolean("first_time", true)) {
            LiseApp.introduction(this);
            settings.edit().putBoolean("first_time", false).commit();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position){
            case 0:
                getFragmentManager().beginTransaction().replace(R.id.container, NewsFragment.newInstance(this)).addToBackStack(null).commit();

        }


    }

    @Override
    public void onDrawerCreate() {
        getFragmentManager().beginTransaction().add(R.id.container,NewsFragment.newInstance(this)).addToBackStack(null).commit();
    }


    @Override
    public void onBackPressed() {
        //Navdrawer
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
            return;
        }

        //searchbar
            android.app.FragmentManager fragmentManager = getFragmentManager();
            if (search!=null) {
                displaySearchView(false);
                return;
            }

        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);

            if(search!=null){
                for (int i = 0; i < menu.size(); i++) {
                    menu.getItem(i).setVisible(false);
                }
            }
            return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.search_action){
            displaySearchView(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
