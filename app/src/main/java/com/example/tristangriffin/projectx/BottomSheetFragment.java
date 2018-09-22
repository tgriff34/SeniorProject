package com.example.tristangriffin.projectx;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private static final String USER_FRAGMENT = "UserFrag";
    //private static final String FAVORITES_FRAGMENT = "FavFrag";
    //private static final String NAVIGATION_FRAGMENT = "NaviFrag";
    //private static final String SEARCH_FRAGMENT = "SearchFrag";
    //private static final String SETTINGS_FRAGMENT = "SettingsFrag";

    public BottomSheetFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        final UserFragment userFragment = (UserFragment) getFragmentManager().findFragmentByTag(USER_FRAGMENT);

        view.findViewById(R.id.action_add_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userFragment.getImages(getActivity(), userFragment.LOCAL_PHOTO_VIEW);
                dismiss();
            }
        });

        view.findViewById(R.id.action_delete_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userFragment.DELETING_IMAGES_FLAG = false;
            }
        });

        view.findViewById(R.id.action_cancel_bottom_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }
}
