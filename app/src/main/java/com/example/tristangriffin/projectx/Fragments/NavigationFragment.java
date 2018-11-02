package com.example.tristangriffin.projectx.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Activities.ImageViewerActivity;
import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Listeners.OnGetAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetPhotosListener;
import com.example.tristangriffin.projectx.Listeners.OnGetPicLatLongListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Models.Image;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Adapters.InfoWindowAdapter;
import com.example.tristangriffin.projectx.Models.InfoWindowData;
import com.example.tristangriffin.projectx.Adapters.NavigationListAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class NavigationFragment extends Fragment implements OnMapReadyCallback {


    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();
    SupportMapFragment supportMapFragment;

    private GoogleMap map;

    private String albumName;
    private ArrayList<Album> albums;
    private RecyclerView recyclerView;

    public static final String PICTURE_SELECT_NAME = "selected-picture";
    public static final String ALBUM_SELECT_NAME = "selected-album";

    private static final int REQUEST_IMAGE_VIEW_CODE = 22;

    public NavigationFragment() {
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
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        //getActivity().setTitle(R.string.navigation_name);
        TextView toolbarTextView = (TextView) ((MainActivity) this.getActivity()).findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.navigation_name);

        //Album List
        recyclerView = view.findViewById(R.id.navigation_list);
        recyclerView.setHasFixedSize(true);
        getAlbums();

        //Create map view
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_layout);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.map_layout, supportMapFragment).commit();
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
        firebaseCommands.getPhotos(albumName, "public", new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(ArrayList<Image> images) {
                setUpMap(images);
            }
        });
    }

    private void getAlbums() {
        //progressBar.setVisibility(View.VISIBLE);
        albums = new ArrayList<>();
        firebaseCommands.getAlbums("public", new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<String> listOfAlbums) {
                if (!listOfAlbums.isEmpty()) {
                    for (int i = 0; i < listOfAlbums.size(); i++){
                        Album album = new Album();
                        album.setName(listOfAlbums.get(i));
                        albums.add(album);
                        getThumbnail(i);
                    }
                } else {
                    updateUI();
                }
            }
        });
    }

    private void getThumbnail(final int position) {
            firebaseCommands.getThumbnail(albums.get(position).getName(), "public", new OnGetThumbnailListener() {
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
        recyclerView.setAdapter(new NavigationListAdapter(getContext(), albums));
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setUpMap(ArrayList<Image> images) {
        //Initializer
        map.clear();

        for (Image image: images) {
            InfoWindowData info = new InfoWindowData();
            info.setName(image.getId());
            info.setImageRef(image.getRef());

            double latitude = Double.parseDouble(image.getLatitude());
            double longitude = Double.parseDouble(image.getLongitude());
            LatLng latLng = new LatLng(latitude, longitude);
            info.setLatLng(latLng);

            DownloadImageAndMakeMarker downloadImageAndMakeMarker = new DownloadImageAndMakeMarker();
            downloadImageAndMakeMarker.execute(info);
        }
    }

    private class DownloadImageAndMakeMarker extends AsyncTask<InfoWindowData, Void, InfoWindowData> {

        Bitmap smallBitmap;

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
            m.setTag(infoWindowData);
        }
    }

}
