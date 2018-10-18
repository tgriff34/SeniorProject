package com.example.tristangriffin.projectx.Fragments;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.R;


public class BottomSheetUserImageFragment extends BottomSheetDialogFragment{

    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();
    private String TAG, album;

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
                firebaseCommands.deleteFromDatabase(TAG, album, "public");
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

    public void setVars(String TAG, String album) {
        this.TAG = TAG;
        this.album = album;
    }
}