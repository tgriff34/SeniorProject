package com.example.tristangriffin.projectx;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextView textFavorites, textNavi, textUser, textSettings, textSearch;
    private FirebaseAuth mAuth;

    private static final String USER_FRAGMENT = "UserFrag";
    private static final String FAVORITES_FRAGMENT = "FavFrag";
    private static final String NAVIGATION_FRAGMENT = "NaviFrag";
    private static final String SEARCH_FRAGMENT = "SearchFrag";
    private static final String SETTINGS_FRAGMENT = "SettingsFrag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            UserFragment userFragment = new UserFragment();
            getSupportFragmentManager().beginTransaction().addToBackStack(null)
                    .add(R.id.fragment_container, userFragment, USER_FRAGMENT).commit();
        }

        textFavorites = (TextView) findViewById(R.id.text_favorites);
        textNavi = (TextView) findViewById(R.id.text_navigation);
        textUser = (TextView) findViewById(R.id.text_user);
        textSettings = (TextView) findViewById(R.id.text_settings);
        textSearch = (TextView) findViewById(R.id.text_search);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_user:
                                UserFragment userFragment = (UserFragment) getSupportFragmentManager()
                                        .findFragmentByTag(USER_FRAGMENT);
                                if (userFragment != null && userFragment.isVisible()) {

                                } else {
                                    UserFragment newUserFragment = new UserFragment();
                                    getSupportFragmentManager().beginTransaction().addToBackStack(null)
                                            .replace(R.id.fragment_container, newUserFragment, USER_FRAGMENT).commit();
                                }

                                textFavorites.setVisibility(View.GONE);
                                textNavi.setVisibility(View.GONE);
                                textSettings.setVisibility(View.GONE);
                                textUser.setVisibility(View.VISIBLE);
                                textUser.setText(mAuth.getCurrentUser().getEmail());
                                textSearch.setVisibility(View.GONE);
                                break;

                            case R.id.action_favorites:
                                textFavorites.setVisibility(View.VISIBLE);
                                textNavi.setVisibility(View.GONE);
                                textSettings.setVisibility(View.GONE);
                                textUser.setVisibility(View.GONE);
                                textSearch.setVisibility(View.GONE);
                                break;

                            case R.id.action_navi:
                                textFavorites.setVisibility(View.GONE);
                                textNavi.setVisibility(View.VISIBLE);
                                textSettings.setVisibility(View.GONE);
                                textUser.setVisibility(View.GONE);
                                textSearch.setVisibility(View.GONE);
                                break;

                            case R.id.action_search:
                                textFavorites.setVisibility(View.GONE);
                                textNavi.setVisibility(View.GONE);
                                textSettings.setVisibility(View.GONE);
                                textUser.setVisibility(View.GONE);
                                textSearch.setVisibility(View.VISIBLE);
                                break;

                            case R.id.action_settings:
                                SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager()
                                        .findFragmentByTag(SETTINGS_FRAGMENT);
                                if (settingsFragment != null && settingsFragment.isVisible()) {

                                } else {
                                    SettingsFragment newSettingsFragment = new SettingsFragment();
                                    getSupportFragmentManager().beginTransaction().addToBackStack(null)
                                            .replace(R.id.fragment_container, newSettingsFragment, SETTINGS_FRAGMENT).commit();
                                }

                                textFavorites.setVisibility(View.GONE);
                                textNavi.setVisibility(View.GONE);
                                textSettings.setVisibility(View.VISIBLE);
                                textUser.setVisibility(View.GONE);
                                textSearch.setVisibility(View.GONE);
                                break;
                        }
                        return false;
                    }
                }
        );
    }
}
