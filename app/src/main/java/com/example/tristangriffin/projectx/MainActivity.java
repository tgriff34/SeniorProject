package com.example.tristangriffin.projectx;

import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private TextView textFavorites, textNavi, textUser, textSettings, textSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
