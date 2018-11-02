package com.example.tristangriffin.projectx.Models;

public class Album {
    String name, thumbnail, id;
    boolean isFavorite, isPublic;

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

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Album{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", isFavorite='" + isFavorite + '\'' +
                ", isPublic='" + isPublic +
                '}';
    }
}
