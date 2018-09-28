package com.example.tristangriffin.projectx;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.example.tristangriffin.projectx.RecyclerViewListAdapter.ALBUM_NAME;


public class UserImageFragment extends Fragment {

    private LinkedHashMap<String, String> cloudImages = new LinkedHashMap<>();
    private GridView gridView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private String albumName;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public static final String DEFAULT_PHOTO_VIEW = "default";

    public UserImageFragment() {
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
        View view = inflater.inflate(R.layout.fragment_user_image, container, false);

        albumName = getArguments().getString(ALBUM_NAME);

        gridView = view.findViewById(R.id.grid_album_view);
        swipeContainer = view.findViewById(R.id.imageSwipeContainer);
        progressBar = view.findViewById(R.id.image_progressbar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        getImages();

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
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getImages();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return view;
    }

    private void getImages() {
        firebaseCommands.getPhotos(new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(LinkedHashMap<String, String> images) {
                updateUI(images);
            }
        }, albumName);
    }

    private void updateUI(LinkedHashMap<String, String> imageArray) {
        cloudImages = imageArray;
        gridView.setAdapter(new GridViewImageAdapter(getActivity(), DEFAULT_PHOTO_VIEW,
                gridView, cloudImages));
        progressBar.setVisibility(View.GONE);
    }
}
