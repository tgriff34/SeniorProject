package com.example.tristangriffin.projectx.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Adapters.SettingsListAdapter;
import com.example.tristangriffin.projectx.R;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {


    private Activity activity;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ArrayList<String> fields = new ArrayList<>();
        fields.add("About");
        fields.add("Dark Theme");
        fields.add("Default Album Privacy");
        fields.add("Sign out");

        RecyclerView recyclerView = view.findViewById(R.id.setting_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SettingsListAdapter(fields, activity));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(activity, R.drawable.recycler_view_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        TextView toolbarTextView = activity.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.settings_name);

        return view;
    }
}
