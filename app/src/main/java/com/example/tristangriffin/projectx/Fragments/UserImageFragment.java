package com.example.tristangriffin.projectx.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Activities.ImageViewerActivity;
import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Models.Image;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Adapters.GridViewImageAdapter;
import com.example.tristangriffin.projectx.Listeners.OnGetPhotosListener;
import com.example.tristangriffin.projectx.R;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.example.tristangriffin.projectx.Activities.MainActivity.ALBUM_NAME;
import static com.example.tristangriffin.projectx.Activities.MainActivity.ALBUM_SELECT_NAME;
import static com.example.tristangriffin.projectx.Activities.MainActivity.PICTURE_SELECT_NAME;
import static com.example.tristangriffin.projectx.Activities.MainActivity.USER_LOCAL_FRAGMENT_TAG;

public class UserImageFragment extends Fragment {

    private ArrayList<Image> cloudImages;
    private GridView gridView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private TextView textInfo;
    private String albumName;
    private Album album;

    private Activity activity;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private static final int REQUEST_IMAGE_VIEW_CODE = 22;

    public UserImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_image, container, false);

        if (getArguments() != null) {
            albumName = getArguments().getString(ALBUM_NAME);
        }

        album = new Gson().fromJson(albumName, Album.class);

        TextView toolbarTextView = activity.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(album.getName());

        textInfo = view.findViewById(R.id.text_noImages);
        gridView = view.findViewById(R.id.grid_album_view);
        swipeContainer = view.findViewById(R.id.imageSwipeContainer);
        progressBar = view.findViewById(R.id.image_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        textInfo.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        getImages();
        setHasOptionsMenu(true);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String value = cloudImages.get(i).getId();
                Log.d("demo", value);
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialog);
                View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_album_list_longpress_layout, null);
                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();

                CardView delete = (CardView) mBottomSheetDialog.findViewById(R.id.album_longpress_action_delete_photos_bottom_sheet);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseCommands.deleteFromDatabase(value, albumName);
                        getImages();
                        mBottomSheetDialog.dismiss();
                    }
                });

                CardView cancel = (CardView) mBottomSheetDialog.findViewById(R.id.album_longpress_action_cancel_bottom_sheet);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBottomSheetDialog.dismiss();
                    }
                });
                return true;
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = cloudImages.get(i).getId();
                Log.d("demo", value);

                Bundle bundle = new Bundle();
                bundle.putString(PICTURE_SELECT_NAME, value);
                bundle.putString(ALBUM_SELECT_NAME, albumName);

                Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_IMAGE_VIEW_CODE);
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getImages();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_options:
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialog);
                View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_album_list_options_layout, null);
                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();

                CardView add = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_add_photos_bottom_sheet);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("album_name", albumName);
                        UserLocalFragment userLocalFragment = new UserLocalFragment();
                        userLocalFragment.setArguments(bundle);
                        ((MainActivity)activity).setFragmentAndTransition(userLocalFragment, USER_LOCAL_FRAGMENT_TAG);
                        mBottomSheetDialog.dismiss();
                    }
                });

                CardView cancel = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_cancel_bottom_sheet);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBottomSheetDialog.dismiss();
                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Private Functions
    private void getImages() {
        cloudImages = new ArrayList<>();
        firebaseCommands.getPhotos(album, new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(ArrayList<Image> images) {
                cloudImages = images;
                updateUI();
            }
        });
    }

    private void updateUI() {
        gridView.setAdapter(new GridViewImageAdapter(getActivity(), cloudImages));
        progressBar.setVisibility(View.GONE);
        if (cloudImages.isEmpty()) {
            textInfo.setVisibility(View.VISIBLE);
        } else {
            textInfo.setVisibility(View.GONE);
        }
    }

    /*
    private void setFragment(Fragment fragment, String TAG) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG)
                .setCustomAnimations(R.anim.fragment_enter_from_right, R.anim.fragment_exit_to_left, R.anim.fragment_enter_from_left, R.anim.fragment_exit_to_right)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragment, TAG)
                .commit();
    }
    */
}
