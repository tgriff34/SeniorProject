package com.example.tristangriffin.projectx.Activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.tristangriffin.projectx.Fragments.FavoritesFragment;
import com.example.tristangriffin.projectx.Fragments.NavigationFragment;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Fragments.SearchFragment;
import com.example.tristangriffin.projectx.Fragments.SettingsFragment;
import com.example.tristangriffin.projectx.Fragments.UserFragment;

public class MainActivity extends AppCompatActivity {

    //Public TAGS
    public static final String USER_FRAGMENT = "UserFrag";
    public static final String FAVORITES_FRAGMENT = "FavFrag";
    public static final String NAVIGATION_FRAGMENT = "NaviFrag";
    public static final String SEARCH_FRAGMENT = "SearchFrag";
    public static final String SETTINGS_FRAGMENT = "SettingsFrag";
    public static final String USER_IMAGE_FRAGMENT_TAG = "UserImageFrag";
    public static final String USER_LOCAL_FRAGMENT_TAG = "UserLocalFrag";
    public static final String ABOUT_FRAGMENT_TAG = "AboutFragment";
    public static final String ALBUM_NAME = "album_name";
    public static final String PICTURE_SELECT_NAME = "selected-picture";
    public static final String ALBUM_SELECT_NAME = "selected-album";

    //Private TAGS
    private static final String BACK_STACK_ROOT_TAG = "root_fragment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentTheme = preferences.getString("current_theme", "Light");
        if (currentTheme.equals("Light")) {
            setTheme(R.style.LightAppTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().popBackStack();
            }
        });


        //Ask permission to access Photos Gallery
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }

        final UserFragment userFragment = new UserFragment();
        final FavoritesFragment favoritesFragment = new FavoritesFragment();
        final NavigationFragment navigationFragment = new NavigationFragment();
        final SearchFragment searchFragment = new SearchFragment();
        final SettingsFragment settingsFragment = new SettingsFragment();

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        setNavigationBarFragment(userFragment, USER_FRAGMENT);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        item.setChecked(true);
                        switch (item.getItemId()) {
                            case R.id.action_user:
                                if (getSupportFragmentManager().findFragmentByTag(USER_FRAGMENT) == null) {
                                    setNavigationBarFragment(userFragment, USER_FRAGMENT);
                                } else {
                                    popToRootFragment();
                                }
                                break;

                            case R.id.action_favorites:
                                if (getSupportFragmentManager().findFragmentByTag(FAVORITES_FRAGMENT) == null) {
                                    setNavigationBarFragment(favoritesFragment, FAVORITES_FRAGMENT);
                                } else {
                                    popToRootFragment();
                                }
                                break;

                            case R.id.action_navi:
                                if (getSupportFragmentManager().findFragmentByTag(NAVIGATION_FRAGMENT) == null) {
                                    setNavigationBarFragment(navigationFragment, NAVIGATION_FRAGMENT);
                                }
                                break;

                            case R.id.action_search:
                                if (getSupportFragmentManager().findFragmentByTag(SEARCH_FRAGMENT) == null) {
                                    setNavigationBarFragment(searchFragment, SEARCH_FRAGMENT);
                                } else {
                                    popToRootFragment();
                                }
                                break;

                            case R.id.action_settings:
                                if (getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT) == null) {
                                    setNavigationBarFragment(settingsFragment, SETTINGS_FRAGMENT);
                                }
                                break;
                        }
                        return false;
                    }
                }
        );
    }

    public void setNavigationBarFragment(Fragment fragment, String FRAGMENT_TAG) {
        getSupportFragmentManager().popBackStackImmediate(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .replace(R.id.fragment_container, fragment, FRAGMENT_TAG)
                .addToBackStack(BACK_STACK_ROOT_TAG)
                .commit();

        checkBackStackEntry();
    }

    public void setFragmentAndTransition(Fragment fragment, String TAG) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG)
                .setCustomAnimations(R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left, R.anim.fragment_enter_from_left, R.anim.fragment_exit_to_right)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragment, TAG)
                .commit();
    }

    public void setFragmentNoTransition(Fragment fragment, String TAG) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(TAG)
                .replace(R.id.fragment_container, fragment, TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            super.finish();
        } else {
            super.onBackPressed();
        }
    }

    //Private Functions
    private void popToRootFragment() {
        for (int i = 1; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void checkBackStackEntry() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });
    }
}
