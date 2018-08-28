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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
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
                                UserFragment userFragment = new UserFragment();
                                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                                        userFragment).commit();

                                textFavorites.setVisibility(View.GONE);
                                textNavi.setVisibility(View.GONE);
                                textSettings.setVisibility(View.GONE);
                                textUser.setVisibility(View.VISIBLE);
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
