package com.example.tristangriffin.projectx.Adapters;

import android.app.Activity;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tristangriffin.projectx.Models.Image;
import com.example.tristangriffin.projectx.R;

import java.util.ArrayList;

public class CheckableGridViewImageAdapter extends BaseAdapter {

    private Activity context;
    private GridView gridView;
    private ArrayList<String> localImages;
    private ArrayList<Image> uploadedImages;

    public CheckableGridViewImageAdapter(Activity mContext, GridView gridView, ArrayList<String> localImages, ArrayList<Image> uploadedImages) {
        context = mContext;
        this.localImages= localImages;
        this.uploadedImages = uploadedImages;
        this.gridView = gridView;
    }

    @Override
    public int getCount() {
        return localImages.size();
    }

    @Override
    public Object getItem(int position) {
        return localImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.checkable_grid_layout, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.checkable_imageView);
        CheckBox checkBox = convertView.findViewById(R.id.checkable_checkBox);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;

        Log.d("demo", uploadedImages.toString());
        Log.d("demo", localImages.toString());

        for (Image image: uploadedImages) {
            Log.d("demo", "Image ID: " + image.getId() + " compared to: " + Uri.parse(localImages.get(position)).getLastPathSegment());
            if (image.getId().equals(Uri.parse(localImages.get(position)).getLastPathSegment())) {
                checkBox.setChecked(true);
                break;
            } else {
                checkBox.setChecked(false);
            }
        }

        if (gridView.isItemChecked(position)) {
            checkBox.setChecked(true);
        }

        imageView.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth / 2, screenWidth / 2));
        imageView.setPadding(12, 12, 12, 12);


        Glide.with(context).load(localImages.get(position)).apply(RequestOptions.centerCropTransform()).into(imageView);

        return convertView;
    }
}
