package com.example.tristangriffin.projectx.Resources;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.example.tristangriffin.projectx.Fragments.NavigationFragment;
import com.example.tristangriffin.projectx.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class NavigationListAdapter extends RecyclerView.Adapter<NavigationListAdapter.MyViewHolder> {

    private LinkedHashMap<String, String> albums;
    private Context context;
    public static final String NAVIGATION_FRAGMENT_TAG = "NaviFrag";

    public NavigationListAdapter(Context context, LinkedHashMap<String, String> albums) {
        this.albums = albums;
        this.context = context;
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
    public void onBindViewHolder(@NonNull NavigationListAdapter.MyViewHolder myViewHolder, int i) {
        final String albumName = new ArrayList<>(albums.keySet()).get(i);
        final TextView textView = myViewHolder.nameTextView;
        ImageView imageView = myViewHolder.holderImageView;

        textView.setText(new ArrayList<>(albums.keySet()).get(i));
        Glide.with(context).load(new ArrayList<>(albums.values()).get(i)).apply(RequestOptions.centerCropTransform()).into(imageView);
        
        
        myViewHolder.holderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationFragment navigationFragment = (NavigationFragment)((FragmentActivity)context).getSupportFragmentManager().findFragmentByTag(NAVIGATION_FRAGMENT_TAG);
                navigationFragment.getAlbumLocations(albumName);
                Toast.makeText(context, albumName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
