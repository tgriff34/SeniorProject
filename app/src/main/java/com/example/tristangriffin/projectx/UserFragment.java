package com.example.tristangriffin.projectx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class UserFragment extends Fragment {

    //private LinkedHashMap<String, String> cloudImages = new LinkedHashMap<>();
    private ArrayList<String> listOfAlbums = new ArrayList<>();
    //private GridView gridView;
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

        //gridView = view.findViewById(R.id.grid_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        progressBar = view.findViewById(R.id.user_progressbar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        getAlbums();

        /*
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = new ArrayList<>(cloudImages.keySet()).get(i);
                Log.d("demo", value);
                BottomSheetUserImageFragment bottomSheetUserImageFragment = new BottomSheetUserImageFragment();
                bottomSheetUserImageFragment.setTAG(value);
                bottomSheetUserImageFragment.show(getFragmentManager(), bottomSheetUserImageFragment.getTag());
                return false;
            }
        });
        */

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
        firebaseCommands.getAlbums(new OnGetDataListener() {
            @Override
            public void onSuccess(LinkedHashMap<String, String> images) {

            }

            @Override
            public void onGetAlbumSuccess(ArrayList<String> albums) {
                updateUI(albums);
            }
        });
        /*
        firebaseCommands.getPhotos(new OnGetDataListener() {
            @Override
            public void onSuccess(LinkedHashMap<String, String> images) {
                updateUI(images);
            }

            @Override
            public void onGetAlbumSuccess(ArrayList<String> albums) {

            }
        });
        */
    }

    /*
    //Update UI async
    private void updateUI(LinkedHashMap<String, String> imageArray) {
        cloudImages = imageArray;
        //gridView.setAdapter(new ImageAdapter(getActivity(), DEFAULT_PHOTO_VIEW));
        gridView.setAdapter(new GridViewImageAdapter(getActivity(), DEFAULT_PHOTO_VIEW,
                gridView, cloudImages));
        progressBar.setVisibility(View.GONE);
    }
    */

    private void updateUI(ArrayList<String> albumArray) {
        listOfAlbums = albumArray;
        Log.d("demo", listOfAlbums.toString());
        recyclerView.setAdapter(new RecyclerViewListAdapter(getContext(), listOfAlbums));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar.setVisibility(View.GONE);
    }
}
