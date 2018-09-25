package com.example.tristangriffin.projectx;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class BottomSheetUserImageFragment extends BottomSheetDialogFragment{

    FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();
    private static final String USER_FRAGMENT = "UserFrag";
    private String TAG;

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

        UserFragment userFragment = (UserFragment) getFragmentManager().findFragmentByTag(USER_FRAGMENT);

        view.findViewById(R.id.action_add_to_collection_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseCommands.addPhotoToCollection(TAG);
                dismiss();
            }
        });

        view.findViewById(R.id.action_delete_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseCommands.deleteFromDatabase(TAG);
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

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }
}
