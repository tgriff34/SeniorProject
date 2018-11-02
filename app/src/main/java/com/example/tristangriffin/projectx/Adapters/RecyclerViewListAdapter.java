package com.example.tristangriffin.projectx.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tristangriffin.projectx.Fragments.UserFragment;
import com.example.tristangriffin.projectx.Fragments.UserImageFragment;
import com.example.tristangriffin.projectx.Listeners.OnDeleteAlbumListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;

import java.util.ArrayList;

import static com.example.tristangriffin.projectx.Activities.MainActivity.USER_FRAGMENT;

public class RecyclerViewListAdapter extends RecyclerView.Adapter<RecyclerViewListAdapter.MyViewHolder> {

    private ArrayList<Album> albums;
    private Context context;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    public static final String USER_IMAGE_FRAGMENT_TAG = "UserImageFrag";
    public static final String ALBUM_NAME = "album_name";

    public RecyclerViewListAdapter(Context context, ArrayList<Album> albums) {
        this.context = context;
        this.albums = albums;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private ImageView holderImageView;
        private Button deleteAlbumButton, favoriteAlbumButton, confirmDeleteAlbumButton, cancelDeleteAlbumButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.album_name);
            holderImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            deleteAlbumButton = (Button) itemView.findViewById(R.id.album_delete_button);
            favoriteAlbumButton = (Button) itemView.findViewById(R.id.album_favorite_button);
            confirmDeleteAlbumButton = (Button) itemView.findViewById(R.id.album_confirm_delete_button);
            cancelDeleteAlbumButton = (Button) itemView.findViewById(R.id.album_cancel_delete_button);
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
    public void onBindViewHolder(@NonNull final RecyclerViewListAdapter.MyViewHolder holder, int position) {
        final String albumName = albums.get(position).getName();
        final TextView textView = holder.nameTextView;
        ImageView imageView = holder.holderImageView;
        final Button favoriteButton = holder.favoriteAlbumButton;

        textView.setText(albumName);
        Glide.with(context).load(albums.get(position).getThumbnail()).apply(RequestOptions.centerCropTransform()).into(imageView);

        /**
         * Is the album favorited?
         */
        if (albums.get(position).isFavorite()) {
            favoriteButton.setText("Unfavorite");
        } else {
            favoriteButton.setText("Favorite");
        }

        /**
         * TODO: ADD DELETE COLLECTION LOGIC
         */
        holder.deleteAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                TranslateAnimation animation = new TranslateAnimation(0, -holder.confirmDeleteAlbumButton.getWidth() * 2, 0, 0);
                animation.setDuration(500);
                animation.setFillAfter(true);
                holder.confirmDeleteAlbumButton.startAnimation(animation);
                */
                /*
                TranslateAnimation animation1 = new TranslateAnimation(0, -holder.cancelDeleteAlbumButton.getWidth(), 0, 0);
                animation.setDuration(500);
                animation.setFillAfter(true);
                holder.cancelDeleteAlbumButton.startAnimation(animation1);
                */

                holder.cancelDeleteAlbumButton.setVisibility(View.VISIBLE);
                holder.confirmDeleteAlbumButton.setVisibility(View.VISIBLE);
                holder.deleteAlbumButton.setVisibility(View.GONE);
                holder.favoriteAlbumButton.setVisibility(View.GONE);

            }
        });

        holder.confirmDeleteAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseCommands.deletePhotoCollection(textView.getText().toString(), "public", new OnDeleteAlbumListener() {
                    @Override
                    public void onDeleteAlbum(boolean isDeleted) {
                        if (isDeleted) {
                            UserFragment userFragment = (UserFragment)((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag(USER_FRAGMENT);
                            userFragment.getAlbums();
                            Toast.makeText(context, "Album deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error deleting album", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        holder.cancelDeleteAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.confirmDeleteAlbumButton.setVisibility(View.GONE);
                holder.cancelDeleteAlbumButton.setVisibility(View.GONE);
                holder.deleteAlbumButton.setVisibility(View.VISIBLE);
                holder.favoriteAlbumButton.setVisibility(View.VISIBLE);
            }
        });

        /**
         * TODO: ADD FAVORITE ALBUM LOGIC
         */
        holder.favoriteAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favoriteButton.getText().equals("Favorite")) {
                    firebaseCommands.favoritePhotoCollection(textView.getText().toString(), "public");
                    favoriteButton.setText("Unfavorite");
                } else {
                    firebaseCommands.unfavoritePhotoCollection(textView.getText().toString(), "public");
                    favoriteButton.setText("Favorite");
                }
            }
        });

        // CLICK ALBUM NAME OR PICTURE
        holder.holderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(ALBUM_NAME, albumName);
                UserImageFragment userImageFragment = new UserImageFragment();
                userImageFragment.setArguments(bundle);
                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left, R.anim.fragment_enter_from_left, R.anim.fragment_exit_to_right)
                        .addToBackStack(USER_IMAGE_FRAGMENT_TAG)
                        .replace(R.id.fragment_container, userImageFragment, USER_IMAGE_FRAGMENT_TAG)
                        .commit();
            }
        });

        // CLICK ALBUM NAME OR PICTURE
        holder.nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(ALBUM_NAME, albumName);
                UserImageFragment userImageFragment = new UserImageFragment();
                userImageFragment.setArguments(bundle);
                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left, R.anim.fragment_enter_from_left, R.anim.fragment_exit_to_right)
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