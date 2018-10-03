package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.rpc.Help;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class RecyclerViewListAdapter extends RecyclerView.Adapter<RecyclerViewListAdapter.MyViewHolder> {

    private LinkedHashMap<String, String> albums;
    private Context context;

    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public static final String USER_IMAGE_FRAGMENT_TAG = "UserImageFrag";
    public static final String ALBUM_NAME = "album_name";

    public RecyclerViewListAdapter(Context mContext, LinkedHashMap<String, String> mAlbums) {
        context = mContext;
        albums = mAlbums;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView nameTextView;
        private ImageView holderImageView;
        private Button deleteAlbumButton, favoriteAlbumButton;
        private ItemClickListener clickListener;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.album_name);
            holderImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            deleteAlbumButton = (Button) itemView.findViewById(R.id.album_delete_button);
            favoriteAlbumButton = (Button) itemView.findViewById(R.id.album_favorite_button);
            itemView.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getLayoutPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onClick(view, getLayoutPosition(), true);
            return true;
        }

    }

    @NonNull
    @Override
    public RecyclerViewListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View albumView = layoutInflater.inflate(R.layout.album_list_layout, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(albumView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewListAdapter.MyViewHolder holder, int position) {
        final String albumName = new ArrayList<>(albums.keySet()).get(position);
        final TextView textView = holder.nameTextView;
        ImageView imageView = holder.holderImageView;

        textView.setText(new ArrayList<>(albums.keySet()).get(position));
        Glide.with(context).load(new ArrayList<>(albums.values()).get(position)).apply(RequestOptions.centerCropTransform()).into(imageView);

        /**
         * TODO: ADD DELETE COLLECTION LOGIC
         */
        holder.deleteAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseCommands.deletePhotoCollection(textView.getText().toString(), "public", new OnDeleteAlbumListener() {
                    @Override
                    public void onDeleteAlbum(boolean isDeleted) {
                        Log.d("demo", "isDeleted: " + isDeleted);
                        if (isDeleted) {
                            Toast.makeText(context, "Album deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Delete photos first", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        /**
         * TODO: ADD FAVORITE ALBUM LOGIC
         */
        holder.favoriteAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.holderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(ALBUM_NAME, albumName);
                UserImageFragment userImageFragment = new UserImageFragment();
                userImageFragment.setArguments(bundle);
                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(USER_IMAGE_FRAGMENT_TAG)
                        .replace(R.id.fragment_container, userImageFragment, USER_IMAGE_FRAGMENT_TAG)
                        .commit();
            }
        });

        holder.nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(ALBUM_NAME, albumName);
                UserImageFragment userImageFragment = new UserImageFragment();
                userImageFragment.setArguments(bundle);
                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(USER_IMAGE_FRAGMENT_TAG)
                        .replace(R.id.fragment_container, userImageFragment, USER_IMAGE_FRAGMENT_TAG)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
