package com.example.tristangriffin.projectx.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tristangriffin.projectx.Listeners.OnGetPicLatLongListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NavigationFragment extends Fragment implements OnMapReadyCallback {


    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();
    SupportMapFragment supportMapFragment;

    private Map<String, double[]> pictureLatLongMap = new HashMap<>();
    private GoogleMap map;

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

        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.map, supportMapFragment).commit();
        }
        supportMapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        getAlbumLocations();
    }

    private void getAlbumLocations() {
        firebaseCommands.getPictureLatLong(new OnGetPicLatLongListener() {
            @Override
            public void getPicLatLong(Map<String, double[]> picInfoMap) {
                pictureLatLongMap = picInfoMap;
                setUpMap();
            }
        });
    }

    private void setUpMap() {
        Iterator iterator = pictureLatLongMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();

            String locationName = pair.getKey().toString();
            double latitude = pictureLatLongMap.get(locationName)[0];
            double longitude = pictureLatLongMap.get(locationName)[1];

            LatLng locationCoordinates = new LatLng(latitude, longitude);

            map.addMarker(new MarkerOptions().position(locationCoordinates).title(locationName));

            iterator.remove();
        }
    }

}
