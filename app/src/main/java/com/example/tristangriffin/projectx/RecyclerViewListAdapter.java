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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewListAdapter extends RecyclerView.Adapter<RecyclerViewListAdapter.MyViewHolder> {

    private ArrayList<String> albums;
    private Context context;

    private static final String USER_IMAGE_FRAGMENT_TAG = "UserImageFrag";
    public static final String ALBUM_NAME = "album_name";

    public RecyclerViewListAdapter(Context mContext, ArrayList<String> mAlbums) {
        context = mContext;
        albums = mAlbums;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener, View.OnLongClickListener {

        private TextView nameTextView;
        private ImageView imageView;
        private ItemClickListener clickListener;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.album_name_text);
            imageView = (ImageView) itemView.findViewById(R.id.image_list_preview);
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
        final String albumName = albums.get(position);
        TextView textView = holder.nameTextView;
        //ImageView imageView = holder.imageView;
        textView.setText(albumName);

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (isLongClick) {
                    //delete album
                } else {
                    Log.d("demo", albums.get(position));
                    Bundle bundle = new Bundle();
                    bundle.putString(ALBUM_NAME, albums.get(position));
                    UserImageFragment userImageFragment = new UserImageFragment();
                    userImageFragment.setArguments(bundle);
                    ((FragmentActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(USER_IMAGE_FRAGMENT_TAG)
                            .replace(R.id.fragment_container, userImageFragment, USER_IMAGE_FRAGMENT_TAG)
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
