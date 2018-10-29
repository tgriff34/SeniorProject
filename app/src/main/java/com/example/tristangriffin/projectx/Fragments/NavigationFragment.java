package com.example.tristangriffin.projectx.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.IDNA;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tristangriffin.projectx.Listeners.OnGetPicLatLongListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Resources.InfoWindowAdapter;
import com.example.tristangriffin.projectx.Resources.InfoWindowData;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NavigationFragment extends Fragment implements OnMapReadyCallback {


    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();
    SupportMapFragment supportMapFragment;

    private Map<String[], double[]> pictureLatLongMap = new HashMap<>();
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

        InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter(getActivity());
        map.setInfoWindowAdapter(infoWindowAdapter);

        getAlbumLocations();
    }

    private void getAlbumLocations() {
        firebaseCommands.getPictureLatLong(new OnGetPicLatLongListener() {
            @Override
            public void getPicLatLong(Map<String[], double[]> picInfoMap) {
                pictureLatLongMap = picInfoMap;
                setUpMap();
            }
        });
    }


    private void setUpMap() {
        //Initializer
        Iterator iterator = pictureLatLongMap.entrySet().iterator();


        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            final String[] locationName = (String[]) pair.getKey();
            double latitude = pictureLatLongMap.get(locationName)[0];
            double longitude = pictureLatLongMap.get(locationName)[1];
            LatLng latLng = new LatLng(latitude, longitude);

            Log.d("demo", "Name: " + locationName[0]);
            Log.d("demo", "Ref: " + locationName[1]);

            InfoWindowData info = new InfoWindowData();
            info.setName(locationName[0]);
            info.setImageRef(locationName[1]);
            info.setLatLng(latLng);

            DownloadImageAndMakeMarker downloadImageAndMakeMarker = new DownloadImageAndMakeMarker();
            downloadImageAndMakeMarker.execute(info);

            iterator.remove();
        }
    }

    private class DownloadImageAndMakeMarker extends AsyncTask<InfoWindowData, Void, InfoWindowData> {

        Bitmap smallBitmap;

        @Override
        protected InfoWindowData doInBackground(InfoWindowData... infoWindowData) {
            try {
                URL url = new URL(infoWindowData[0].getImageRef());
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                smallBitmap = bitmap.createScaledBitmap(bitmap, 100, 100, false);
                infoWindowData[0].setImage(smallBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return infoWindowData[0];
        }

        @Override
        protected void onPostExecute(InfoWindowData infoWindowData) {
            MarkerOptions options = new MarkerOptions();
            options.position(infoWindowData.getLatLng());
            Marker m = map.addMarker(options);
            m.setTag(infoWindowData);
        }
    }

}
