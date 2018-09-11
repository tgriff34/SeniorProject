package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class UserFragment extends Fragment implements View.OnClickListener {

    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<String> checkedImages = new ArrayList<>();
    private GridView gridView;
    private TextView _textPhotoAdd;
    private FloatingActionButton _photoAdd;
    private MaterialButton _photoConfirm;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String DEFAULT_PHOTO_VIEW = "default";
    private static final String LOCAL_PHOTO_VIEW = "local";

    public UserFragment() {
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
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        gridView = (GridView) view.findViewById(R.id.grid_view);
        _photoConfirm = view.findViewById(R.id.button_confirmPhoto);
        _photoAdd = view.findViewById(R.id.button_addPhoto);
        _textPhotoAdd = view.findViewById(R.id.text_addPhoto);

        if (images != null && !images.isEmpty()) {
            _textPhotoAdd.setVisibility(View.GONE);
        } else {
            getImages(getActivity(), DEFAULT_PHOTO_VIEW);
        }

        _photoConfirm.setOnClickListener(this);
        _photoAdd.setOnClickListener(this);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageAdapter adapter = (ImageAdapter) gridView.getAdapter();
                if (images != null && !images.isEmpty()) {
                    adapter.notifyDataSetChanged();
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_addPhoto:
                getImages(getActivity(), LOCAL_PHOTO_VIEW);
                if (gridView.getAdapter().isEmpty()) {

                } else {
                    _textPhotoAdd.setVisibility(View.GONE);
                    _photoAdd.hide();
                    _photoConfirm.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.button_confirmPhoto:
                getCheckedImages();
                uploadImages();
                break;
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private Activity context;
        private String tag;

        public ImageAdapter(Activity mContext, String TAG) {
            context = mContext;
            this.tag = TAG;
        }

        @Override
        public int getCount() {
            return images.size();
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

            Glide.with(context).load(images.get(i)).apply(RequestOptions.centerCropTransform()).into(imageView);

            return imageView;
        }
    }

    private void getImages(Activity activity, String TAG) {

        final ArrayList<String> allImages = new ArrayList<>();
        final String mTAG = TAG;

        //When you want to add photos from local hdd
        if (TAG == LOCAL_PHOTO_VIEW) {
            String[] columns = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
            Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    columns, null, null, null);

            while (cursor.moveToNext()) {
                allImages.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
            }

            Log.d("demo", "Local images: " + allImages.toString());

            updateUI(allImages, mTAG);
        }

        //When you want to view current photos on cloud (default view)
        if (TAG == DEFAULT_PHOTO_VIEW) {
            db.collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .collection("images")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    allImages.add(document.getString("ref"));
                                }
                                Log.d("demo", "Cloud images: " + allImages.toString());
                                updateUI(allImages, mTAG);
                            }
                        }
                    });
        }
    }

    private ArrayList<String> getCheckedImages() {
        SparseBooleanArray a = gridView.getCheckedItemPositions();
        for (int i = 0; i < a.size(); i++) {
            if (a.valueAt(i)) {
                int index = a.keyAt(i);
                checkedImages.add(images.get(index));
            }
        }
        return checkedImages;
    }


    //Uploads files to Firebase Storage
    private void uploadImages() {
        //Go through checked images
        for (String image : checkedImages) {
            Uri file = Uri.fromFile(new File(image));
            new UploadFilesTask().execute(file);
        }
    }

    private void updateUI(ArrayList<String> imageArray, String TAG) {
        images = imageArray;
        gridView.setAdapter(new ImageAdapter(getActivity(),TAG));
    }
}
