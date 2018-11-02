package com.example.tristangriffin.projectx.Adapters;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tristangriffin.projectx.Resources.CheckableImageView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.example.tristangriffin.projectx.Fragments.UserFragment.DEFAULT_PHOTO_VIEW;
import static com.example.tristangriffin.projectx.Fragments.UserLocalFragment.LOCAL_PHOTO_VIEW;

public class GridViewImageAdapter extends BaseAdapter {
    private Activity context;
    private GridView gridView;
    private String TAG;
    private ArrayList<String> images;
    private LinkedHashMap<String, String> cloudImages;

    public GridViewImageAdapter(Activity mContext, String mTAG, GridView mGridView, ArrayList<String> mImages) {
        context = mContext;
        gridView = mGridView;
        TAG = mTAG;
        images = mImages;
    }

    public GridViewImageAdapter(Activity mContext, String mTAG, GridView mGridView,
                                 LinkedHashMap<String, String> mCloudImages) {
        context = mContext;
        gridView = mGridView;
        TAG = mTAG;
        cloudImages = mCloudImages;
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

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int screenWidth = metrics.widthPixels;

            imageView.setLayoutParams(new GridView.LayoutParams(screenWidth / 2, screenWidth / 2));
            imageView.setPadding(12,12,12,12);
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
