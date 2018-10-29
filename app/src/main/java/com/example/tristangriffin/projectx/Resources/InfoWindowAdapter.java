package com.example.tristangriffin.projectx.Resources;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tristangriffin.projectx.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public InfoWindowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.map_info_window, null);

        TextView photoName = view.findViewById(R.id.map_info_textView);
        ImageView photo = view.findViewById(R.id.map_info_imageView);

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        photoName.setText(infoWindowData.getName());
        photo.setImageBitmap(infoWindowData.getImage());

        return view;
    }
}
