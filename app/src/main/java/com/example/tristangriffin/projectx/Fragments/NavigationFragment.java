package com.example.tristangriffin.projectx.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
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

            MyTasksParams params = new MyTasksParams(locationName[0], locationName[1], latLng);
            Log.d("demo", "LatLng: " +params.latLng.toString());
            Log.d("demo", "Location Name: " + locationName[0]);
            Log.d("demo", "Image Ref: " + locationName[1]);

            DownloadImageAndMakeMarker downloadImageAndMakeMarker = new DownloadImageAndMakeMarker();
            downloadImageAndMakeMarker.execute(params);

            iterator.remove();
        }
    }

    private static class MyTasksParams {
        String locationName;
        String imageRef;
        LatLng latLng;

        MyTasksParams(String locationName, String imageRef, LatLng latLng) {
            this.locationName = locationName;
            this.imageRef = imageRef;
            this.latLng = latLng;
        }
    }

    private class DownloadImageAndMakeMarker extends AsyncTask<MyTasksParams, Void, Void> {

        String locationName;
        LatLng latLng;
        Bitmap smallBitmap;

        @Override
        protected Void doInBackground(MyTasksParams... myTasksParams) {
            try {
                locationName = myTasksParams[0].locationName;
                URL url = new URL(myTasksParams[0].imageRef);
                latLng = myTasksParams[0].latLng;
                Log.d("demo", latLng.toString());
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                smallBitmap = bitmap.createScaledBitmap(bitmap, 100, 100, false);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("demo", latLng.toString());
            map.addMarker(new MarkerOptions().position(latLng).title(locationName).icon(BitmapDescriptorFactory.fromBitmap(smallBitmap)));
        }
    }

}
