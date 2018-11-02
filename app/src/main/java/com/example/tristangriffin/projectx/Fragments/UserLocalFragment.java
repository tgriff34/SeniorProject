package com.example.tristangriffin.projectx.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Resources.GeoLocationConverter;
import com.example.tristangriffin.projectx.Adapters.GridViewImageAdapter;
import com.example.tristangriffin.projectx.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class UserLocalFragment extends Fragment {

    private ArrayList<String> images = new ArrayList<>();
    private GridView gridView;
    private String latitude = null, longitude = null,
            timeCreated = null, dateCreated = null, location = null;
    private Uri file;
    private String albumName;
    private Geocoder geocoder;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public static final String LOCAL_PHOTO_VIEW = "local";
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 353;

    public UserLocalFragment() {
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
        View view = inflater.inflate(R.layout.fragment_user_local, container, false);

        getActivity().setTitle(R.string.local_photos_name);

        gridView = view.findViewById(R.id.grid_local_view);
        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        albumName = getArguments().getString("album_name");
        setHasOptionsMenu(true);
        getImages(getActivity());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                uploadImage(images.get(position));
            }
        });

        return view;
    }

    public void getImages(Activity activity) {
        ArrayList<String> allImages = new ArrayList<>();
        String[] columns = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, null);

        while (cursor.moveToNext()) {
            allImages.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
        }
        cursor.close();
        updateUI(allImages);
    }

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

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            if (latitude.equals("0.0") && longitude.equals("0.0")) {
                GET_LOCATION_FLAG = true;
                //Open up fragment to type location (Google Place Autocomplete)
                builder.setTitle("Set Location");
                builder.setMessage("This image does not have a location associated with." +
                        "  Would you like to associate a location with this image?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(getContext(), "Image not added to album.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.show();
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
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            location = addresses.get(0).getAddressLine(0);
            firebaseCommands.uploadPhotos(file, albumName, location, longitude, latitude, timeCreated, dateCreated);
        }
    }

    private void updateUI(ArrayList<String> imageArray) {
        images = imageArray;
        gridView.setAdapter(new GridViewImageAdapter(getActivity(), LOCAL_PHOTO_VIEW,
                gridView, images));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.done_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                GridViewImageAdapter adapter = (GridViewImageAdapter) gridView.getAdapter();
                adapter.notifyDataSetChanged();

                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                latitude = String.valueOf(place.getLatLng().latitude);
                longitude = String.valueOf(place.getLatLng().longitude);
                location = String.valueOf(place.getAddress());
                firebaseCommands.uploadPhotos(file, albumName, location, longitude, latitude, timeCreated, dateCreated);
                Log.d("UserLocalFragment", place.getLatLng().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Toast.makeText(getContext(), "Request Failed", Toast.LENGTH_SHORT).show();
                Log.d("UserLocalFragment", "Something went wrong....");
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                Log.d("UserLocalFragment", "User cancelled place picker.");
            }
        }
    }
}
