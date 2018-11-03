package com.example.tristangriffin.projectx.Adapters;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tristangriffin.projectx.Models.Image;
import com.example.tristangriffin.projectx.R;

import java.util.ArrayList;

public class GridViewImageAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<Image> cloudImages;

    public GridViewImageAdapter(Activity mContext, ArrayList<Image> mCloudImages) {
        context = mContext;
        cloudImages = mCloudImages;
    }

    @Override
    public int getCount() {
        return cloudImages.size();
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
        //TODO: Possibly make it more columns / smaller images

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_layout, viewGroup, false);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.grid_imageView);

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        int screenWidth = metrics.widthPixels;
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth / 2, screenWidth / 2));
        imageView.setPadding(12, 12, 12, 12);

        Glide.with(context).load(cloudImages.get(i).getRef()).apply(RequestOptions.centerCropTransform()).into(imageView);

        return view;
    }
}
