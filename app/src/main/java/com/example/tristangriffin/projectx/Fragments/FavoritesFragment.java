package com.example.tristangriffin.projectx.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Adapters.RecyclerViewCompactListAdapter;
import com.example.tristangriffin.projectx.Listeners.OnGetFavoritedAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    private ArrayList<Album> favoritedAlbums;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textView;

    private Activity activity;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private SharedPreferences preferences;

    public FavoritesFragment() {
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

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        //getActivity().setTitle(R.string.favorites_name);
        TextView toolbarTextView = activity.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.favorites_name);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recyclerView = view.findViewById(R.id.favorites_recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.favorites_swipeContainer);
        progressBar = view.findViewById(R.id.favorites_progressBar);
        textView = view.findViewById(R.id.favorites_textInfo);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);

        getFavoriteAlbums();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFavoriteAlbums();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    public void getFavoriteAlbums() {
        progressBar.setVisibility(View.VISIBLE);
        favoritedAlbums = new ArrayList<>();
        firebaseCommands.getFavoritedPhotoCollection(new OnGetFavoritedAlbumListener() {
            @Override
            public void getFavoritedAlbum(ArrayList<Album> albums) {
                if (!albums.isEmpty()) {
                    for (int i = 0; i < albums.size(); i++) {
                        favoritedAlbums.add(albums.get(i));
                        getThumbnail(i);
                    }
                } else {
                    updateUI();
                }
            }
        });
    }

    private void getThumbnail(final int position) {
        firebaseCommands.getThumbnail(favoritedAlbums.get(position), new OnGetThumbnailListener() {
            @Override
            public void onGetThumbnailSuccess(String string) {
                favoritedAlbums.get(position).setThumbnail(string);
                updateUI();
            }
        });
    }

    private void updateUI() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String currentView = preferences.getString("view_size", "Large");
        if (currentView.equals("Large")) {
            recyclerView.setAdapter(new RecyclerViewListAdapter(activity, favoritedAlbums));
        } else {
            recyclerView.setAdapter(new RecyclerViewCompactListAdapter(activity, favoritedAlbums));
        }

        progressBar.setVisibility(View.GONE);

        if (favoritedAlbums.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}
