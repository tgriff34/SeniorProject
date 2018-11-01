package com.example.tristangriffin.projectx.Fragments;

import android.annotation.SuppressLint;
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
import android.widget.Toolbar;

import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Listeners.OnGetSearchAlbumsListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.RecyclerViewListAdapter;

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

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        //getActivity().setTitle(R.string.search_name);
        TextView toolbarTextView = (TextView) ((MainActivity) this.getActivity()).findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.search_name);

        recyclerView = view.findViewById(R.id.search_recyclerView);
        swipeContainer = view.findViewById(R.id.search_swipeContainer);
        progressBar = view.findViewById(R.id.search_progressBar);
        searchText = view.findViewById(R.id.search_editText);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((!searchText.getText().toString().equals("") && event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (actionId == EditorInfo.IME_ACTION_DONE))) {
                    getAlbums(searchText.getText().toString());
                }
                return false;
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("demo", "Search String: " + searchText.getText().toString());
                if (!searchText.getText().toString().equals("")) {
                    getAlbums(searchText.getText().toString());
                }
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    private void getAlbums(String searchString) {
        progressBar.setVisibility(View.VISIBLE);
        searchedAlbums = new LinkedHashMap<>();
        firebaseCommands.searchAlbums(searchString, new OnGetSearchAlbumsListener() {
            @Override
            public void searchedAlbums(ArrayList<String> albums) {
                Log.d("demo", "Searched Albums: " + albums);
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
