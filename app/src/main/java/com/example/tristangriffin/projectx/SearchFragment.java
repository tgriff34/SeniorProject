package com.example.tristangriffin.projectx;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SearchFragment extends Fragment {

    private LinkedHashMap<String, String> searchedAlbums;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private EditText searchText;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public SearchFragment() {
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.search_recyclerView);
        swipeContainer = view.findViewById(R.id.search_swipeContainer);
        progressBar = view.findViewById(R.id.search_progressBar);
        searchText = view.findViewById(R.id.search_editText);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (actionId == EditorInfo.IME_ACTION_DONE))) {
                    getAlbums(searchText.getText().toString());
                }
                return false;
            }
        });


        return view;
    }

    private void getAlbums(String searchString) {
        progressBar.setVisibility(View.VISIBLE);
        searchedAlbums = new LinkedHashMap<>();
        firebaseCommands.searchAlbums(searchString, new OnGetSearchAlbumsListener() {
            @Override
            public void searchedAlbums(ArrayList<String> albums) {
                Log.d("demo", "Searched Albums: " + albums);
                getThumbnail(albums);
            }
        });
    }

    private void getThumbnail(final ArrayList<String> albums) {
        for (int i = 0; i < albums.size(); i++ ) {
            final int j = i;
            /**
             * Fix for public and private
             */
            firebaseCommands.getThumbnail(albums.get(i), "public", new OnGetThumbnailListener() {
                @Override
                public void onGetThumbnailSuccess(String string) {
                    searchedAlbums.put(albums.get(j), string);
                    updateUI();
                }
            });
        }
    }

    private void updateUI() {
        Log.d("demo", "Searched images: " + searchedAlbums.toString());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerViewListAdapter(getContext(), searchedAlbums));
        progressBar.setVisibility(View.GONE);
    }
}
