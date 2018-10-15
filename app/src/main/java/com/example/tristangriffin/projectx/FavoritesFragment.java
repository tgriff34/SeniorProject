package com.example.tristangriffin.projectx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FavoritesFragment extends Fragment {

    private LinkedHashMap<String, String> favoritedImages;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textView;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public FavoritesFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

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

    private void getFavoriteAlbums() {
        progressBar.setVisibility(View.VISIBLE);
        favoritedImages = new LinkedHashMap<>();
        firebaseCommands.getFavoritedPhotoCollection(new OnGetFavoritedAlbumListener() {
            @Override
            public void getFavoritedAlbum(ArrayList<String> albums) {
                if (!albums.isEmpty()) {
                    getThumbnail(albums);
                } else {
                    updateUI();
                }
            }
        });
    }

    private void getThumbnail(final ArrayList<String> albums) {
        for (int i = 0; i < albums.size(); i++ ) {
            final int j = i;
            firebaseCommands.getThumbnail(albums.get(i), "public", new OnGetThumbnailListener() {
                @Override
                public void onGetThumbnailSuccess(String string) {
                    favoritedImages.put(albums.get(j), string);
                    updateUI();
                }
            });
        }
    }

    private void updateUI() {
        Log.d("demo", "Favorite images: " + favoritedImages.toString());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerViewListAdapter(getContext(), favoritedImages));
        progressBar.setVisibility(View.GONE);

        if (favoritedImages.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}
