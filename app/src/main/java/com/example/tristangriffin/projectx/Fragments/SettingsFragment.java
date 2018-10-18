package com.example.tristangriffin.projectx.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tristangriffin.projectx.Activities.SignInActivity;
import com.example.tristangriffin.projectx.R;
import com.google.firebase.auth.FirebaseAuth;


public class SettingsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView listView;

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

        listView = (ListView) view.findViewById(R.id.list_settings);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String currentTheme = sharedPreferences.getString("current_theme", "Light");
                if (currentTheme.equals("Light")) {
                    sharedPreferences.edit().putString("current_theme", "Dark").apply();
                } else {
                    sharedPreferences.edit().putString("current_theme", "Light").apply();
                }
                getActivity().recreate();
                break;

            case 1:
                Toast.makeText(getActivity(), "Signed Out", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
        }
    }
}