package com.example.tristangriffin.projectx;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class UserFragment extends Fragment {

    private LinkedHashMap<String, String> cloudImages = new LinkedHashMap<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public static final String DEFAULT_PHOTO_VIEW = "default";

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        progressBar = view.findViewById(R.id.user_progressbar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        getAlbums();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAlbums();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    private void getAlbums() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseCommands.getAlbums(new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<String> albums) {
                getThumbnail(albums);
            }
        });
    }

    private void getThumbnail(final ArrayList<String> albums) {
        for (int i = 0; i < albums.size(); i++ ) {
            final int j = i;
            firebaseCommands.getThumbnail(new OnGetThumbnailListener() {
                @Override
                public void onGetThumbnailSuccess(String string) {
                    cloudImages.put(albums.get(j), string);
                    updateUI();
                }
            }, albums.get(i));
        }
    }

    private void updateUI() {
        Log.d("demo", "Cloud images: " + cloudImages.toString());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerViewListAdapter(getContext(), cloudImages));
        progressBar.setVisibility(View.GONE);
    }
}
