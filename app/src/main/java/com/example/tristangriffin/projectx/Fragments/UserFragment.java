package com.example.tristangriffin.projectx.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Listeners.OnGetIfFavoritedAlbumListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Listeners.OnGetAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter;

import java.util.ArrayList;

public class UserFragment extends Fragment {

    //private LinkedHashMap<String, String> cloudImages;
    private ArrayList<Album> albums;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private TextView textView;

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

        //getActivity().setTitle(R.string.album_name);
        TextView toolbarTextView = (TextView) ((MainActivity) this.getActivity()).findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.album_name);

        recyclerView = view.findViewById(R.id.recycler_view);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        progressBar = view.findViewById(R.id.user_progressbar);
        textView = view.findViewById(R.id.user_textInfo);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);
        textView.setVisibility(View.GONE);

        getAlbums();
        setHasOptionsMenu(true);

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

    public void getAlbums() {
        progressBar.setVisibility(View.VISIBLE);
        //cloudImages = new LinkedHashMap<>();
        albums = new ArrayList<>();
        firebaseCommands.getAlbums(new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<String> listOfAlbums) {
                if (!listOfAlbums.isEmpty()) {
                    for (int i = 0; i < listOfAlbums.size(); i++) {
                        Album newAlbum = new Album();
                        newAlbum.setName(listOfAlbums.get(i));
                        albums.add(newAlbum);
                        getThumbnail(i);
                    }
                } else {
                    updateUI();
                }
            }
        });
    }

    //Private Functions
    private void getThumbnail(final int position) {
        firebaseCommands.getThumbnail(albums.get(position).getName(), new OnGetThumbnailListener() {
            @Override
            public void onGetThumbnailSuccess(String string) {
                albums.get(position).setThumbnail(string);
                getIsFavorite(position);
            }
        });
    }

    private void getIsFavorite(final int position) {
        firebaseCommands.getIfFavoritedPhotoCollection(albums.get(position).getName(), new OnGetIfFavoritedAlbumListener() {
            @Override
            public void getIfFavoritedAlbumListener(boolean isFavorite) {
                Log.d("demo", "isFavorite: " + isFavorite);
                albums.get(position).setFavorite(isFavorite);
                updateUI();
            }
        });
    }

    private void updateUI() {
        Log.d("demo", "Albums: " + albums.toString());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerViewListAdapter(getActivity(), albums));
        progressBar.setVisibility(View.GONE);

        if (albums.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.add_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
