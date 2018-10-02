package com.example.tristangriffin.projectx;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.example.tristangriffin.projectx.BottomSheetFragment.USER_LOCAL_FRAGMENT_TAG;
import static com.example.tristangriffin.projectx.RecyclerViewListAdapter.USER_IMAGE_FRAGMENT_TAG;


public class BottomSheetUserImageFragment extends BottomSheetDialogFragment{

    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();
    private String TAG, album;
    private boolean deleteAlbumIfEmpty = false;

    public BottomSheetUserImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_image_layout, container, false);

        view.findViewById(R.id.action_add_to_collection_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //firebaseCommands.addPhotoToCollection(TAG, album);
                dismiss();
            }
        });

        view.findViewById(R.id.action_delete_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseCommands.deleteFromDatabase(TAG, album, deleteAlbumIfEmpty);
                dismiss();
            }
        });

        view.findViewById(R.id.action_cancel_bottom_sheet_images).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }

    public void setVars(String TAG, String album, boolean deleteAlbumIfEmpty) {
        this.TAG = TAG;
        this.album = album;
        this.deleteAlbumIfEmpty = deleteAlbumIfEmpty;
    }
}
