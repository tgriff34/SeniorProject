package com.example.tristangriffin.projectx.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Activities.ImageViewerActivity;
import com.example.tristangriffin.projectx.Listeners.OnGetAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetMapMarkerListener;
import com.example.tristangriffin.projectx.Listeners.OnGetPhotosListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Models.Image;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Adapters.InfoWindowAdapter;
import com.example.tristangriffin.projectx.Models.InfoWindowData;
import com.example.tristangriffin.projectx.Adapters.NavigationListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.tristangriffin.projectx.Activities.MainActivity.ALBUM_SELECT_NAME;
import static com.example.tristangriffin.projectx.Activities.MainActivity.PICTURE_SELECT_NAME;

public class NavigationFragment extends Fragment implements OnMapReadyCallback {


    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();
    SupportMapFragment supportMapFragment;

    private static GoogleMap map;

    private String albumName;
    private ArrayList<Album> albums;
    private RecyclerView recyclerView;
    private String selectedAlbum = null;

    private LatLngBounds.Builder builder;

    private Activity activity;

    private static final int REQUEST_IMAGE_VIEW_CODE = 22;

    public NavigationFragment() {
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
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        //getActivity().setTitle(R.string.navigation_name);
        TextView toolbarTextView = activity.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.navigation_name);

        Bundle arguments = getArguments();
        if (arguments != null) {
            selectedAlbum = getArguments().getString("selectedAlbum");
        }

        //Album List
        recyclerView = view.findViewById(R.id.navigation_list);
        recyclerView.setHasFixedSize(true);
        getAlbums();

        //Create map view
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_layout);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction().replace(R.id.map_layout, supportMapFragment).commit();
            }
        }
        supportMapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        final InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter(getActivity());
        map.setInfoWindowAdapter(infoWindowAdapter);

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String value = marker.getTitle();
                Log.d("demo", value);

                Bundle bundle = new Bundle();
                bundle.putString(PICTURE_SELECT_NAME, value);
                bundle.putString(ALBUM_SELECT_NAME, albumName);

                Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_IMAGE_VIEW_CODE);
            }
        });
    }

    //Private Funcs
    public void getAlbumLocations(String album) {
        albumName = album;
        firebaseCommands.getPhotos(albumName, new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(ArrayList<Image> images) {
                setUpMap(images);
            }
        });
    }

    private void getAlbums() {
        //progressBar.setVisibility(View.VISIBLE);
        albums = new ArrayList<>();
        firebaseCommands.getAlbums(new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<Album> listOfAlbums) {
                if (!listOfAlbums.isEmpty()) {
                    for (int i = 0; i < listOfAlbums.size(); i++) {
                        albums.add(listOfAlbums.get(i));
                        getThumbnail(i);
                    }
                } else {
                    updateUI();
                }
            }
        });
    }

    private void getThumbnail(final int position) {
        firebaseCommands.getThumbnail(albums.get(position).getName(), new OnGetThumbnailListener() {
            @Override
            public void onGetThumbnailSuccess(String string) {
                albums.get(position).setThumbnail(string);
                updateUI();
            }
        });
    }

    private void updateUI() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setAdapter(new NavigationListAdapter(getContext(), albums, selectedAlbum));
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setUpMap(ArrayList<Image> images) {
        //Initializer
        map.clear();

        builder = new LatLngBounds.Builder();

        for (int i = 0; i < images.size(); i++) {
            final int position = i;
            final int finalPositon = images.size();
            final Image image = images.get(i);
            InfoWindowData info = new InfoWindowData();
            info.setName(image.getId());
            info.setImageRef(image.getRef());

            double latitude = Double.parseDouble(image.getLatitude());
            double longitude = Double.parseDouble(image.getLongitude());
            LatLng latLng = new LatLng(latitude, longitude);
            info.setLatLng(latLng);

            DownloadImageAndMakeMarker downloadImageAndMakeMarker = new DownloadImageAndMakeMarker(new OnGetMapMarkerListener() {
                @Override
                public void onGetMapMarker(Marker marker) {
                    builder.include(marker.getPosition());
                    if (position == finalPositon - 1) {
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
                    }
                }
            });
            downloadImageAndMakeMarker.execute(info);
        }

    }

    private static class DownloadImageAndMakeMarker extends AsyncTask<InfoWindowData, Void, InfoWindowData> {

        Bitmap smallBitmap;

        private OnGetMapMarkerListener delegate;

        private DownloadImageAndMakeMarker(OnGetMapMarkerListener delegate) {
            this.delegate = delegate;
        }

        @Override
        protected InfoWindowData doInBackground(InfoWindowData... infoWindowData) {
            try {
                URL url = new URL(infoWindowData[0].getImageRef());
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                smallBitmap = bitmap.createScaledBitmap(bitmap, 150, 150, false);
                infoWindowData[0].setImage(smallBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return infoWindowData[0];
        }

        @Override
        protected void onPostExecute(InfoWindowData infoWindowData) {
            MarkerOptions options = new MarkerOptions();
            options.position(infoWindowData.getLatLng()).title(infoWindowData.getName());
            Marker m = map.addMarker(options);
            delegate.onGetMapMarker(m);
            m.setTag(infoWindowData);
        }
    }
}
