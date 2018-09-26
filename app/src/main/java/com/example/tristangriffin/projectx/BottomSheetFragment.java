package com.example.tristangriffin.projectx;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    //private static final String USER_FRAGMENT = "UserFrag";
    private static final String USER_LOCAL_FRAGMENT_TAG = "UserLocalFrag";
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


        view.findViewById(R.id.action_add_photos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserLocalFragment userLocalFragment = new UserLocalFragment();
                setFragment(userLocalFragment, USER_LOCAL_FRAGMENT_TAG);
                dismiss();
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

    private void setFragment(Fragment fragment, String TAG) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragment, TAG)
                .commit();

    }
}
