package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
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
import java.util.ArrayList;

public class UserFragment extends Fragment implements View.OnClickListener {

    private ArrayList<String> images = new ArrayList<>();
    private GridView gridView;
    private int width;


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
        view.findViewById(R.id.button_addPhoto).setOnClickListener(this);

        gridView = (GridView) view.findViewById(R.id.grid_view);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (images != null && !images.isEmpty()) {
                    Toast.makeText(getContext(), images.get(i).toString(), Toast.LENGTH_SHORT).show();
                    gridView.setItemChecked(i, true);
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {
        gridView.setAdapter(new ImageAdapter(getActivity()));
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
            ImageView imageView;
            if (view == null) {
                imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new GridView.LayoutParams(500 , 500));
            } else {
                imageView = (ImageView) view;
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
}
