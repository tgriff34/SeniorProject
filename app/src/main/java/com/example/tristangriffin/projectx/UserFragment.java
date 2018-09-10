package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.database.Cursor;
import android.media.Image;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class UserFragment extends Fragment implements View.OnClickListener {

    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<String> checkedImages = new ArrayList<>();
    private GridView gridView;
    private TextView _textPhotoAdd;
    private FloatingActionButton _photoAdd;
    private MaterialButton _photoConfirm;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

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
        switch(view.getId()) {
            case R.id.button_addPhoto:
                gridView.setAdapter(new ImageAdapter(getActivity()));
                if (gridView.getAdapter().isEmpty()) {

                } else {
                    _textPhotoAdd.setVisibility(View.GONE);
                    _photoAdd.setVisibility(View.GONE);
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

        public ImageAdapter(Activity mContext) {
            context = mContext;
            images = getImages(context);
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
                imageView.setLayoutParams(new GridView.LayoutParams(500 , 500));
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

    private ArrayList<String> getImages(Activity activity) {
        ArrayList<String> allImages = new ArrayList<>();
        String[] columns = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };
        Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, null);

        while (cursor.moveToNext()){
            allImages.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
        }

        Log.d("demo", allImages.toString());

        return allImages;
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

    private void uploadImages() {
        for (String image: checkedImages) {
            Uri file = Uri.fromFile(new File(image));
            StorageReference fileRef = storageReference.child("images/public/" + file.getLastPathSegment());
            UploadTask uploadTask = fileRef.putFile(file);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Firebase", "uploadSuccess: true");
                }
            }). addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Firebase", "uploadSuccess: false", e);
                }
            });
        }
    }
}
