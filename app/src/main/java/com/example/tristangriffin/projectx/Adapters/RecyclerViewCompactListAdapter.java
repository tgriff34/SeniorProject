package com.example.tristangriffin.projectx.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Fragments.NavigationFragment;
import com.example.tristangriffin.projectx.Fragments.UserFragment;
import com.example.tristangriffin.projectx.Fragments.UserImageFragment;
import com.example.tristangriffin.projectx.Fragments.UserLocalFragment;
import com.example.tristangriffin.projectx.Listeners.OnDeleteAlbumListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;

import java.util.ArrayList;

import static com.example.tristangriffin.projectx.Activities.MainActivity.NAVIGATION_FRAGMENT;
import static com.example.tristangriffin.projectx.Activities.MainActivity.USER_FRAGMENT;

public class RecyclerViewCompactListAdapter extends RecyclerView.Adapter<RecyclerViewCompactListAdapter.MyViewHolder> {

    //Parameters
    private ArrayList<Album> albums;
    private Activity activity;

    //CardView views
    private TextView favoriteText;
    private ImageView favoriteImage;

    private BottomNavigationView bottomNavigationView;

    //Firebase commands
    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();


    //TAGS
    private static final String USER_IMAGE_FRAGMENT_TAG = "UserImageFrag";
    private static final String USER_LOCAL_FRAGMENT_TAG = "UserLocalFrag";
    private static final String ALBUM_NAME = "album_name";

    public RecyclerViewCompactListAdapter(Activity activity, ArrayList<Album> albums) {
        this.activity = activity;
        this.albums = albums;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView nameTextView;
        private ImageView imageView;
        private ImageButton optionsButton, favoriteButton;

        private MyViewHolder(@Nullable View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            nameTextView = (TextView) itemView.findViewById(R.id.album_compact_textView);
            imageView = (ImageView) itemView.findViewById(R.id.album_compact_imageView);
            optionsButton = (ImageButton) itemView.findViewById(R.id.album_compact_options_button);
            favoriteButton = (ImageButton) itemView.findViewById(R.id.album_compact_favorite_button);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putString(ALBUM_NAME, albums.get(getAdapterPosition()).getName());
            UserImageFragment userImageFragment = new UserImageFragment();
            userImageFragment.setArguments(bundle);
            ((MainActivity) activity).setFragmentAndTransition(userImageFragment, USER_IMAGE_FRAGMENT_TAG);
        }
    }

    @NonNull
    @Override
    public RecyclerViewCompactListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View albumView = layoutInflater.inflate(R.layout.album_compact_list_layout, viewGroup, false);

        //MyViewHolder viewHolder = new MyViewHolder(albumView);

        return new MyViewHolder(albumView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewCompactListAdapter.MyViewHolder myViewHolder, int i) {
        final int position = myViewHolder.getAdapterPosition();
        final TextView textView = (TextView) myViewHolder.nameTextView;
        ImageView imageView = (ImageView) myViewHolder.imageView;
        ImageButton optionsButton = (ImageButton) myViewHolder.optionsButton;
        final ImageButton favoriteButton = (ImageButton) myViewHolder.favoriteButton;

        final String albumName = albums.get(position).getName();

        textView.setText(albumName);

        if (albums.get(position).getThumbnail() != null) {
            Glide.with(activity).load(albums.get(position).getThumbnail()).apply(RequestOptions.centerCropTransform()).into(imageView);
        }

        bottomNavigationView = ((MainActivity) activity).findViewById(R.id.bottom_navigation);

        //Change color of buttons depending on theme
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String theme = preferences.getString("current_theme", "Light");
        if (theme.equals("Light")) {
            optionsButton.setColorFilter(activity.getResources().getColor(R.color.colorPrimary, null), PorterDuff.Mode.SRC_IN);
            favoriteButton.setColorFilter(activity.getResources().getColor(R.color.colorPrimary, null), PorterDuff.Mode.SRC_IN);
        } else {
            optionsButton.setColorFilter(activity.getResources().getColor(R.color.offWhite, null), PorterDuff.Mode.SRC_IN);
            favoriteButton.setColorFilter(activity.getResources().getColor(R.color.offWhite, null), PorterDuff.Mode.SRC_IN);
        }

        //Set favorite Resources
        getFavoriteResourcesForRecyclerView(favoriteButton, position);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(ALBUM_NAME, albumName);
                UserImageFragment userImageFragment = new UserImageFragment();
                userImageFragment.setArguments(bundle);
                ((MainActivity) activity).setFragmentAndTransition(userImageFragment, USER_IMAGE_FRAGMENT_TAG);
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavorite(position);
                getFavoriteResourcesForRecyclerView(favoriteButton, position);
            }
        });

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialog);
                View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_album_longpress_layout, null);
                mBottomSheetDialog.setContentView(sheetView);

                CardView cancel = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_cancel_bottom_sheet);
                CardView delete = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_delete_bottom_sheet);
                CardView addPhotos = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_addPhotos_bottom_sheet);
                CardView showMap = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_viewOnMap_bottom_sheet);
                CardView favorite = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_favorite_bottom_sheet);
                favoriteText = favorite.findViewById(R.id.album_view_favorite_text_bottom_sheet);
                favoriteImage = favorite.findViewById(R.id.album_view_favorite_image_bottom_sheet);

                getFavoriteResourcesForCardView(position);

                mBottomSheetDialog.show();

                addPhotos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("album_name", albumName);
                        UserLocalFragment userLocalFragment = new UserLocalFragment();
                        userLocalFragment.setArguments(bundle);
                        ((MainActivity) activity).setFragmentAndTransition(userLocalFragment, USER_LOCAL_FRAGMENT_TAG);
                        mBottomSheetDialog.dismiss();
                    }
                });

                favorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setFavorite(position);
                        mBottomSheetDialog.dismiss();
                    }
                });

                showMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("selectedAlbum", albumName);
                        NavigationFragment navigationFragment = new NavigationFragment();
                        navigationFragment.setArguments(bundle);
                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                        ((MainActivity) activity).setNavigationBarFragment(navigationFragment, NAVIGATION_FRAGMENT);
                        mBottomSheetDialog.dismiss();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseCommands.deletePhotoCollection(textView.getText().toString(), new OnDeleteAlbumListener() {
                            @Override
                            public void onDeleteAlbum(boolean isDeleted) {
                                if (isDeleted) {
                                    UserFragment userFragment = (UserFragment) ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(USER_FRAGMENT);
                                    userFragment.getAlbums();
                                    Toast.makeText(activity, "Album deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, "Error deleting album", Toast.LENGTH_SHORT).show();
                                }
                                mBottomSheetDialog.dismiss();
                            }
                        });
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBottomSheetDialog.dismiss();
                    }
                });
            }
        });
    }

    //Private Functions
    private void getFavoriteResourcesForRecyclerView(ImageButton imageButton, int position) {
        if (albums.get(position).isFavorite()) {
            imageButton.setImageResource(R.drawable.ic_heart_closed);
        } else {
            imageButton.setImageResource(R.drawable.ic_heart);
        }
    }

    private void getFavoriteResourcesForCardView(int position) {
        if (albums.get(position).isFavorite()) {
            //CardView
            favoriteText.setText("Favorite");
            favoriteImage.setImageResource(R.drawable.ic_heart);
        } else {
            //CardView
            favoriteText.setText("Unfavorite");
            favoriteImage.setImageResource(R.drawable.ic_heart_closed);
        }
    }

    private void setFavorite(int position) {
        firebaseCommands.favoritePhotoCollection(albums.get(position));
        albums.get(position).setFavorite(!albums.get(position).isFavorite());
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
