package com.example.tristangriffin.projectx.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Activities.SignInActivity;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.google.firebase.auth.FirebaseAuth;


public class SettingsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private SharedPreferences sharedPreferences;

    private static final String ABOUT_FRAGMENT_TAG = "AboutFragment";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        //getActivity().setTitle(R.string.settings_name);
        TextView toolbarTextView = (TextView) ((MainActivity) this.getActivity()).findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.settings_name);

        ListView listView = (ListView) view.findViewById(R.id.list_settings);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                AboutFragment aboutFragment = new AboutFragment();
                setFragment(aboutFragment, ABOUT_FRAGMENT_TAG);
                break;

            case 1:
                String currentTheme = sharedPreferences.getString("current_theme", "Light");
                if (currentTheme.equals("Light")) {
                    sharedPreferences.edit().putString("current_theme", "Dark").apply();
                } else {
                    sharedPreferences.edit().putString("current_theme", "Light").apply();
                }

                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
                break;

            case 2:
                String privacySetting = sharedPreferences.getString("current_privacy", "Public");
                if (privacySetting.equals("Public")) {
                    sharedPreferences.edit().putString("current_privacy", "Private").apply();
                    Toast.makeText(getContext(), "Private", Toast.LENGTH_SHORT).show();
                } else {
                    sharedPreferences.edit().putString("current_privacy", "Public").apply();
                    Toast.makeText(getContext(), "Public", Toast.LENGTH_SHORT).show();
                }

                break;

            case 3:
                Toast.makeText(getActivity(), "Signed Out", Toast.LENGTH_SHORT).show();
                FirebaseCommands.getInstance().signOut();
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
        }
    }

    private void setFragment(Fragment fragment, String TAG) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG)
                .setCustomAnimations(R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left, R.anim.fragment_enter_from_left, R.anim.fragment_exit_to_right)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragment, TAG)
                .commit();
    }
}
