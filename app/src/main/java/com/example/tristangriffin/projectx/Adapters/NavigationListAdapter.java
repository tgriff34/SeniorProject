package com.example.tristangriffin.projectx.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tristangriffin.projectx.Fragments.NavigationFragment;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class NavigationListAdapter extends RecyclerView.Adapter<NavigationListAdapter.MyViewHolder> {

    private ArrayList<Album> albums;
    private Context context;
    private String selectedAlbum;

    private int lastCheckPos = -1;

    public static final String NAVIGATION_FRAGMENT_TAG = "NaviFrag";

    public NavigationListAdapter(Context context, ArrayList<Album> albums, @Nullable String selectedAlbum) {
        this.albums = albums;
        this.context = context;
        this.selectedAlbum = selectedAlbum;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private ImageView holderImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.navigation_list_textView);
            holderImageView = (ImageView) itemView.findViewById(R.id.navigation_list_imageView);
        }
    }

    @NonNull
    @Override
    public NavigationListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View albumView = layoutInflater.inflate(R.layout.navigation_list_layout, viewGroup, false);

        MyViewHolder viewHolder = new MyViewHolder(albumView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final NavigationListAdapter.MyViewHolder myViewHolder, int position) {
        final Album album = albums.get(position);
        final String albumName = albums.get(position).getName();
        final TextView textView = myViewHolder.nameTextView;
        final ImageView imageView = myViewHolder.holderImageView;

        textView.setText(albumName);
        Glide.with(context).load(albums.get(position).getThumbnail()).apply(RequestOptions.circleCropTransform()).into(imageView);

        if (album.getName().equals(selectedAlbum)) {
            album.setSelected(true);
            lastCheckPos = myViewHolder.getAdapterPosition();
            NavigationFragment navigationFragment = (NavigationFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag(NAVIGATION_FRAGMENT_TAG);
            navigationFragment.getAlbumLocations(albumName);
            selectedAlbum = null;
        }

        imageView.setBackgroundResource(album.isSelected() ? R.drawable.navigation_image_view_selected_border : R.drawable.navigation_image_view_border);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lastCheckPos >= 0) {
                    albums.get(lastCheckPos).setSelected(false);
                }

                album.setSelected(!album.isSelected());

                imageView.setBackgroundResource(album.isSelected() ? R.drawable.navigation_image_view_selected_border : R.drawable.navigation_image_view_border);

                lastCheckPos = myViewHolder.getAdapterPosition();

                NavigationFragment navigationFragment = (NavigationFragment) ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag(NAVIGATION_FRAGMENT_TAG);
                navigationFragment.getAlbumLocations(albumName);
                Toast.makeText(context, albumName, Toast.LENGTH_SHORT).show();

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
