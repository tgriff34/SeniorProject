package com.example.tristangriffin.projectx.Models;

public class Album {
    String name, thumbnail;
    boolean isFavorite;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return "Album{" +
                "name='" + name + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
