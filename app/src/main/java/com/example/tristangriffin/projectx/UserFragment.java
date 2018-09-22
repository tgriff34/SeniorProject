package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
    private String latitude = null, longitude = null, timeCreated = null, dateCreated = null;
    private Uri file;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private static final String DEFAULT_PHOTO_VIEW = "default";
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
        outState.putStringArrayList("list", images);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        gridView = view.findViewById(R.id.grid_view);

        //Retrieve old display list when fragment changed
        if (savedInstanceState != null) {
            images = savedInstanceState.getStringArrayList("list");
        } else {
            getImages(getActivity(), DEFAULT_PHOTO_VIEW);
        }

       /**
        *   TODO: Set flag
        *   Setup some sort of flag for the listener to differentiate between adding,
        *   deleting, etc...
        *   Right now whenever you click it uploads
        */
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageAdapter adapter = (ImageAdapter) gridView.getAdapter();
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

        return view;
    }

    private class ImageAdapter extends BaseAdapter {
        private Activity context;
        private String TAG;

        private ImageAdapter(Activity mContext, String mTAG) {
            context = mContext;
            TAG = mTAG;
        }

        @Override
        public int getCount() {
            switch (TAG) {
                case LOCAL_PHOTO_VIEW:
                    return images.size();

                case DEFAULT_PHOTO_VIEW:
                    return cloudImages.size();

                default:
                    return -1;
            }
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CheckableImageView imageView;
            //TODO: Possibly make it more columns / smaller images
            if (view == null) {
                imageView = new CheckableImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new GridView.LayoutParams(500, 500));
            } else {
                imageView = (CheckableImageView) view;
            }

            if (gridView.isItemChecked(i)) {
                imageView.setChecked(true);
            } else {
                imageView.setChecked(false);
            }

            if (TAG.equals(LOCAL_PHOTO_VIEW)) {
                Glide.with(context).load(images.get(i)).apply(RequestOptions.centerCropTransform()).into(imageView);
            } else if (TAG.equals(DEFAULT_PHOTO_VIEW)) {
                Glide.with(context).load(new ArrayList<>(cloudImages.values()).get(i)).apply(RequestOptions.centerCropTransform()).into(imageView);
            }

            return imageView;
        }
    }

    public void getImages(Activity activity, String TAG) {
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

        //When you want to view current photos on cloud (default view)
        if (TAG.equals(DEFAULT_PHOTO_VIEW)) {
            firebaseCommands.getPhotos(new OnGetDataListener() {
                @Override
                public void onSuccess(LinkedHashMap<String, String> images) {
                    updateUI(images);
                }
            });
            ADDING_IMAGES_FLAG = false;
        }
    }

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
            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
            firebaseCommands.uploadPhotos(file, longitude, latitude, timeCreated, dateCreated);
        }
    }

    //Update UI async
    private void updateUI(ArrayList<String> imageArray) {
        images = imageArray;
        gridView.setAdapter(new ImageAdapter(getActivity(), LOCAL_PHOTO_VIEW));
    }

    //Update UI async
    private void updateUI(LinkedHashMap<String, String> imageArray) {
        cloudImages = imageArray;
        gridView.setAdapter(new ImageAdapter(getActivity(), DEFAULT_PHOTO_VIEW));
    }

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
                Log.d("PLACES", "Something went wrong....");
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("PLACES", "User cancelled place picker.");
            }
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }
}
