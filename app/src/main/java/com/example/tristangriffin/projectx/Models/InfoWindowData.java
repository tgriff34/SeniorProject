package com.example.tristangriffin.projectx.Models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

public class InfoWindowData {

    private Bitmap bitmap;
    private String name, imageRef;
    private LatLng latLng;

    public Bitmap getImage() {
        return bitmap;
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getImageRef() {
        return imageRef;
    }

    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
    }
}
