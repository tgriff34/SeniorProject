package com.example.tristangriffin.projectx.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.tristangriffin.projectx.Fragments.FavoritesFragment;
import com.example.tristangriffin.projectx.Listeners.OnGetAlbumListener;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Fragments.NavigationFragment;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Fragments.SearchFragment;
import com.example.tristangriffin.projectx.Fragments.SettingsFragment;
import com.example.tristangriffin.projectx.Fragments.UserFragment;
import com.example.tristangriffin.projectx.Fragments.UserImageFragment;

import java.util.ArrayList;

import static com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter.ALBUM_NAME;
import static com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter.USER_IMAGE_FRAGMENT_TAG;

public class MainActivity extends AppCompatActivity {

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();


    public static final String USER_FRAGMENT = "UserFrag";
    public static final String FAVORITES_FRAGMENT = "FavFrag";
    public static final String NAVIGATION_FRAGMENT = "NaviFrag";
    public static final String SEARCH_FRAGMENT = "SearchFrag";
    public static final String SETTINGS_FRAGMENT = "SettingsFrag";

    private static final String EXTRAS = "EXTRAS";

    private boolean isPublic = false;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        checkBackStackEntry();


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

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        setFragment(userFragment, USER_FRAGMENT);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_user:
                                item.setChecked(true);
                                setFragment(userFragment, USER_FRAGMENT);
                                break;

                            case R.id.action_favorites:
                                item.setChecked(true);
                                setFragment(favoritesFragment, FAVORITES_FRAGMENT);
                                break;

                            case R.id.action_navi:
                                item.setChecked(true);
                                setFragment(navigationFragment, NAVIGATION_FRAGMENT);
                                break;

                            case R.id.action_search:
                                item.setChecked(true);
                                setFragment(searchFragment, SEARCH_FRAGMENT);
                                break;

                            case R.id.action_settings:
                                item.setChecked(true);
                                setFragment(settingsFragment, SETTINGS_FRAGMENT);
                                break;
                        }
                        return false;
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_done:
                getSupportFragmentManager().popBackStackImmediate();
                return true;

            case R.id.action_add_collection:
                final AlertDialog builder = new AlertDialog.Builder(MainActivity.this).create();
                View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.text_input_add_album, null);
                final EditText input = (EditText) viewInflated.findViewById(R.id.album_input);

                builder.setView(viewInflated);
                builder.setCancelable(true);

                Button okButton = (Button) viewInflated.findViewById(R.id.builder_yes_button);
                Button noButton = (Button) viewInflated.findViewById(R.id.builder_no_button);

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String isPublicString = preferences.getString("current_privacy", "Public");
                        isPublic = (isPublicString.equals("Public"));

                        firebaseCommands.createPhotoCollection(input.getText().toString(), isPublic);

                        Bundle bundle = new Bundle();
                        bundle.putString(ALBUM_NAME, input.getText().toString());
                        UserImageFragment userImageFragment = new UserImageFragment();
                        userImageFragment.setArguments(bundle);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .addToBackStack(USER_IMAGE_FRAGMENT_TAG)
                                .replace(R.id.fragment_container, userImageFragment, USER_IMAGE_FRAGMENT_TAG)
                                .commit();

                        builder.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });
                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Private Functions
    private void setFragment(Fragment fragment, String FRAGMENT_TAG) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .replace(R.id.fragment_container, fragment, FRAGMENT_TAG)
                .commit();
    }

    private void checkBackStackEntry() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });
    }
}
