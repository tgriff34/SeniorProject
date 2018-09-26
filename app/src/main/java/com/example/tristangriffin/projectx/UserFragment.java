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

    private ArrayList<String> images = new ArrayList<>();
    private LinkedHashMap<String, String> cloudImages = new LinkedHashMap<>();
    private GridView gridView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private String latitude = null, longitude = null, timeCreated = null, dateCreated = null;
    private Uri file;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public static final String DEFAULT_PHOTO_VIEW = "default";
    public static final String LOCAL_PHOTO_VIEW = "local";

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 353;

    public boolean ADDING_IMAGES_FLAG = false;
    public boolean DELETING_IMAGES_FLAG = false;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putStringArrayList("list", images);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        gridView = view.findViewById(R.id.grid_view);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        progressBar = view.findViewById(R.id.user_progressbar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        //Retrieve old display list when fragment changed
        /*
        if (savedInstanceState != null) {
            images = savedInstanceState.getStringArrayList("list");
        } else {
            getImages(getActivity(), DEFAULT_PHOTO_VIEW);
        }
        */

        getImages(getActivity(), DEFAULT_PHOTO_VIEW);

        /**
         *   TODO: Set flag
         *   Setup some sort of flag for the listener to differentiate between adding,
         *   deleting, etc...
         *   Right now whenever you click it uploads
         */
        /*
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //ImageAdapter adapter = (ImageAdapter) gridView.getAdapter();
                GridViewImageAdapter adapter = (GridViewImageAdapter) gridView.getAdapter();
                if (ADDING_IMAGES_FLAG) {
                    if (images != null && !images.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        uploadImage(images.get(i));
                    }
                } else if (DELETING_IMAGES_FLAG) {
                    if (images != null && !images.isEmpty()) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        */

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
                if (ADDING_IMAGES_FLAG) {
                    getImages(getActivity(), LOCAL_PHOTO_VIEW);
                } else {
                    getImages(getActivity(), DEFAULT_PHOTO_VIEW);
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

    public void getImages(Activity activity, String TAG) {
        /*
        final ArrayList<String> allImages = new ArrayList<>();
        //When you want to add photos from local hdd
        if (TAG.equals(LOCAL_PHOTO_VIEW)) {
            String[] columns = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
            Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    columns, null, null, null);

            while (cursor.moveToNext()) {
                allImages.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
            }
            cursor.close();
            Log.d("demo", "Local images: " + allImages.toString());
            ADDING_IMAGES_FLAG = true;
            updateUI(allImages);
        }
        */

        //When you want to view current photos on cloud (default view)
        //if (TAG.equals(DEFAULT_PHOTO_VIEW)) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseCommands.getPhotos(new OnGetDataListener() {
            @Override
            public void onSuccess(LinkedHashMap<String, String> images) {
                updateUI(images);
            }
        });
        ADDING_IMAGES_FLAG = false;
        //}
    }

    /*
    //Uploads files to Firebase Storage
    private void uploadImage(String image) {
        file = Uri.fromFile(new File(image));

        boolean GET_LOCATION_FLAG = false;

        //Add Exif Here
        String stringFile = file.getPath();
        try {
            ExifInterface exifInterface = new ExifInterface(stringFile);

            //Location
            GeoLocationConverter location = new GeoLocationConverter(exifInterface);
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            if (latitude.equals("0.0") && longitude.equals("0.0")) {
                GET_LOCATION_FLAG = true;
                //Open up fragment to type location (Google Place Autocomplete)
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());

                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
            }

            //Time Created
            timeCreated = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            dateCreated = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //End of Exif
        if (!GET_LOCATION_FLAG) {
            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
            firebaseCommands.uploadPhotos(file, longitude, latitude, timeCreated, dateCreated);
        }
    }

    //Update UI async
    private void updateUI(ArrayList<String> imageArray) {
        images = imageArray;
        //gridView.setAdapter(new ImageAdapter(getActivity(), LOCAL_PHOTO_VIEW));
        gridView.setAdapter(new GridViewImageAdapter(getActivity(), LOCAL_PHOTO_VIEW,
                gridView, images));
    }
    */

    //Update UI async
    private void updateUI(LinkedHashMap<String, String> imageArray) {
        cloudImages = imageArray;
        //gridView.setAdapter(new ImageAdapter(getActivity(), DEFAULT_PHOTO_VIEW));
        gridView.setAdapter(new GridViewImageAdapter(getActivity(), DEFAULT_PHOTO_VIEW,
                gridView, cloudImages));
        progressBar.setVisibility(View.GONE);
    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                latitude = String.valueOf(place.getLatLng().latitude);
                longitude = String.valueOf(place.getLatLng().longitude);
                firebaseCommands.uploadPhotos(file, longitude, latitude, timeCreated, dateCreated);
                Log.d("demo", place.getLatLng().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                //Fails with Genymotion
                Toast.makeText(getContext(), "Request Failed", Toast.LENGTH_SHORT).show();
                Log.d("PLACES", "Something went wrong....");
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                Log.d("PLACES", "User cancelled place picker.");
            }
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }
    */
}
