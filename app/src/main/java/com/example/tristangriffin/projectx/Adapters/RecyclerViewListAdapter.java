package com.example.tristangriffin.projectx.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
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

import static com.example.tristangriffin.projectx.Activities.MainActivity.ALBUM_NAME;
import static com.example.tristangriffin.projectx.Activities.MainActivity.NAVIGATION_FRAGMENT;
import static com.example.tristangriffin.projectx.Activities.MainActivity.USER_FRAGMENT;
import static com.example.tristangriffin.projectx.Activities.MainActivity.USER_IMAGE_FRAGMENT_TAG;
import static com.example.tristangriffin.projectx.Activities.MainActivity.USER_LOCAL_FRAGMENT_TAG;

public class RecyclerViewListAdapter extends RecyclerView.Adapter<RecyclerViewListAdapter.MyViewHolder> {

    private ArrayList<Album> albums;

    private Activity activity;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();



    public RecyclerViewListAdapter(Activity activity, ArrayList<Album> albums) {
        this.albums = albums;
        this.activity = activity;
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
        final int i = holder.getAdapterPosition();
        final String albumName = albums.get(position).getName();
        final TextView textView = holder.nameTextView;
        ImageView imageView = holder.holderImageView;
        final Button favoriteButton = holder.favoriteAlbumButton;

        textView.setText(albumName);
        if (albums.get(position).getThumbnail() != null) {
            Glide.with(activity).load(albums.get(position).getThumbnail()).apply(RequestOptions.centerCropTransform()).into(imageView);
        }

        final BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);

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
                    firebaseCommands.favoritePhotoCollection(albums.get(i));
                    albums.get(i).setFavorite(true);
                    favoriteButton.setText("Unfavorite");
                } else {
                    firebaseCommands.favoritePhotoCollection(albums.get(i));
                    albums.get(i).setFavorite(false);
                    favoriteButton.setText("Favorite");
                }
            }
        });

        // CLICK ALBUM NAME OR PICTURE
        holder.holderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(albumName);
            }
        });

        // CLICK ALBUM NAME OR PICTURE
        holder.nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(albumName);
            }
        });

        // LONG CLICK / BOTTOM SHEET
        holder.holderImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialog);
                View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_album_longpress_layout, null);
                mBottomSheetDialog.setContentView(sheetView);

                CardView cancel = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_cancel_bottom_sheet);
                CardView delete = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_delete_bottom_sheet);
                CardView addPhotos = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_addPhotos_bottom_sheet);
                CardView showMap = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_viewOnMap_bottom_sheet);
                CardView favorite = (CardView) mBottomSheetDialog.findViewById(R.id.album_view_favorite_bottom_sheet);
                TextView favoriteText = favorite.findViewById(R.id.album_view_favorite_text_bottom_sheet);
                ImageView favoriteImage=  favorite.findViewById(R.id.album_view_favorite_image_bottom_sheet);

                if (favoriteButton.getText().equals("Favorite")) {
                    favoriteText.setText("Favorite");
                    favoriteImage.setImageResource(R.drawable.ic_heart);
                } else {
                    favoriteText.setText("Unfavorite");
                    favoriteImage.setImageResource(R.drawable.ic_heart_closed);
                }

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
                        if (favoriteButton.getText().equals("Favorite")) {
                            firebaseCommands.favoritePhotoCollection(albums.get(i));
                            albums.get(i).setFavorite(true);
                            favoriteButton.setText("Unfavorite");
                        } else {
                            firebaseCommands.favoritePhotoCollection(albums.get(i));
                            albums.get(i).setFavorite(false);
                            favoriteButton.setText("Favorite");
                        }
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
                        //((MainActivity) context).setFragment(navigationFragment, NAVIGATION_FRAGMENT);
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

                return true;
            }
        });
    }

    //Private Functions
    private void setFragment(String albumName) {
        Bundle bundle = new Bundle();
        bundle.putString(ALBUM_NAME, albumName);
        UserImageFragment userImageFragment = new UserImageFragment();
        userImageFragment.setArguments(bundle);
        ((MainActivity) activity).setFragmentAndTransition(userImageFragment, USER_IMAGE_FRAGMENT_TAG);
    }

    // Required Functions
    @Override
    public int getItemCount() {
        return albums.size();
    }
}
