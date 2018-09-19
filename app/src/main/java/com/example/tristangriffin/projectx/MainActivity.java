package com.example.tristangriffin.projectx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private static final String USER_FRAGMENT = "UserFrag";
    private static final String FAVORITES_FRAGMENT = "FavFrag";
    private static final String NAVIGATION_FRAGMENT = "NaviFrag";
    private static final String SEARCH_FRAGMENT = "SearchFrag";
    private static final String SETTINGS_FRAGMENT = "SettingsFrag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask permission to access Photos Gallery
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            return;
        }

        final UserFragment userFragment = new UserFragment();
        final FavoritesFragment favoritesFragment = new FavoritesFragment();
        final NavigationFragment navigationFragment = new NavigationFragment();
        final SearchFragment searchFragment = new SearchFragment();
        final SettingsFragment settingsFragment = new SettingsFragment();

        if (firebaseCommands.user == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            setFragment(userFragment, USER_FRAGMENT);
        }

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_user:
                                setFragment(userFragment, USER_FRAGMENT);
                                break;

                            case R.id.action_favorites:
                                setFragment(favoritesFragment, FAVORITES_FRAGMENT);
                                break;

                            case R.id.action_navi:
                                setFragment(navigationFragment, NAVIGATION_FRAGMENT);
                                break;

                            case R.id.action_search:
                                setFragment(searchFragment, SEARCH_FRAGMENT);
                                break;

                            case R.id.action_settings:
                                setFragment(settingsFragment, SETTINGS_FRAGMENT);
                                break;
                        }
                        return false;
                    }
                }
        );
    }

    private void setFragment(Fragment fragment, String FRAGMENT_TAG) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragment, FRAGMENT_TAG)
                .commit();
    }
}
