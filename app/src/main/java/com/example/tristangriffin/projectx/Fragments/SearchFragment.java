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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Adapters.RecyclerViewCompactListAdapter;
import com.example.tristangriffin.projectx.Listeners.OnGetIfFavoritedAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetUsersListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Models.User;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Listeners.OnGetSearchAlbumsListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private ArrayList<Album> searchedAlbums;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private EditText searchText;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private Activity activity;

    private SharedPreferences preferences;

    public SearchFragment() {
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        //getActivity().setTitle(R.string.search_name);
        TextView toolbarTextView = activity.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.search_name);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
                    searchAlbums(searchText.getText().toString());
                }
                return false;
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("demo", "Search String: " + searchText.getText().toString());
                if (!searchText.getText().toString().equals("")) {
                    searchAlbums(searchText.getText().toString());
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

    private void searchAlbums(final String searchString) {
        Log.d("demo", "Searching...");
        progressBar.setVisibility(View.VISIBLE);
        searchedAlbums = new ArrayList<>();
        firebaseCommands.searchUserAlbums(searchString, new OnGetSearchAlbumsListener() {
            @Override
            public void searchedAlbums(Album album, int position) {
                if (album != null) {
                    searchedAlbums.add(position, album);
                    getThumbnail(album, position);
                } else {
                    updateUI();
                }
            }
        });

        firebaseCommands.getUsers(new OnGetUsersListener() {
            @Override
            public void onGetUsers(ArrayList<User> users) {
                Log.d("demo", "Users: " + users.toString());
                for (int i = 0; i < users.size(); i++) {
                    firebaseCommands.searchPublicAlbums(users.get(i), searchString, new OnGetSearchAlbumsListener() {
                        @Override
                        public void searchedAlbums(Album album, int position) {
                            if (album != null) {
                                searchedAlbums.add(position, album);
                                getThumbnail(album, position);
                            } else {
                                updateUI();
                            }
                        }
                    });
                }
            }
        });
    }

    private void getThumbnail(final Album album, final int position) {
        firebaseCommands.getThumbnail(album, new OnGetThumbnailListener() {
            @Override
            public void onGetThumbnailSuccess(String string) {
                searchedAlbums.get(position).setThumbnail(string);
                updateUI();
            }
        });
    }

    private void updateUI() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String currentView = preferences.getString("view_size", "Large");
        if (currentView.equals("Large")) {
            recyclerView.setAdapter(new RecyclerViewListAdapter(activity, searchedAlbums));
        } else {
            recyclerView.setAdapter(new RecyclerViewCompactListAdapter(activity, searchedAlbums));
        }

        progressBar.setVisibility(View.GONE);
    }

}
