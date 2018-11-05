package com.example.tristangriffin.projectx.Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Adapters.RecyclerViewCompactListAdapter;
import com.example.tristangriffin.projectx.Listeners.OnGetIfFavoritedAlbumListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Listeners.OnGetAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter.ALBUM_NAME;
import static com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter.USER_IMAGE_FRAGMENT_TAG;

public class UserFragment extends Fragment {

    private ArrayList<Album> albums;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private TextView textView;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private SharedPreferences preferences;
    private String privacySetting;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        //getActivity().setTitle(R.string.album_name);
        final Toolbar toolbar = (Toolbar) ((MainActivity) this.getActivity()).findViewById(R.id.toolbar);
        TextView toolbarTextView = (TextView) ((MainActivity) this.getActivity()).findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.album_name);



        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recyclerView = view.findViewById(R.id.recycler_view);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        progressBar = view.findViewById(R.id.user_progressbar);
        textView = view.findViewById(R.id.user_textInfo);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);
        textView.setVisibility(View.GONE);

        getAlbums();
        setHasOptionsMenu(true);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAlbums();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    public void getAlbums() {
        progressBar.setVisibility(View.VISIBLE);
        //cloudImages = new LinkedHashMap<>();
        albums = new ArrayList<>();
        firebaseCommands.getAlbums(new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<String> listOfAlbums) {
                if (!listOfAlbums.isEmpty()) {
                    for (int i = 0; i < listOfAlbums.size(); i++) {
                        Album newAlbum = new Album();
                        newAlbum.setName(listOfAlbums.get(i));
                        albums.add(newAlbum);
                        getThumbnail(i);
                    }
                } else {
                    updateUI();
                }
            }
        });
    }

    //Private Functions
    private void getThumbnail(final int position) {
        firebaseCommands.getThumbnail(albums.get(position).getName(), new OnGetThumbnailListener() {
            @Override
            public void onGetThumbnailSuccess(String string) {
                albums.get(position).setThumbnail(string);
                getIsFavorite(position);
            }
        });
    }

    private void getIsFavorite(final int position) {
        firebaseCommands.getIfFavoritedPhotoCollection(albums.get(position).getName(), new OnGetIfFavoritedAlbumListener() {
            @Override
            public void getIfFavoritedAlbumListener(boolean isFavorite) {
                Log.d("demo", "isFavorite: " + isFavorite);
                albums.get(position).setFavorite(isFavorite);
                updateUI();
            }
        });
    }

    private void updateUI() {
        Log.d("demo", "Albums: " + albums.toString());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String currentView = preferences.getString("view_size", "Large");
        if (currentView.equals("Large")) {
            recyclerView.setAdapter(new RecyclerViewListAdapter(getActivity(), albums));
        } else {
            recyclerView.setAdapter(new RecyclerViewCompactListAdapter(getActivity(), albums));
        }

        progressBar.setVisibility(View.GONE);

        if (albums.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void sortBy(String string) {
        if (string.equals("Ascending")) {
            Collections.sort(albums, new Comparator<Album>() {
                @Override
                public int compare(Album o1, Album o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        } else {
            Collections.sort(albums, new Comparator<Album>() {
                @Override
                public int compare(Album o1, Album o2) {
                    return o2.getName().compareTo(o1.getName());
                }
            });
        }

        updateUI();
    }

    private void createSortByBottomSheet() {
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.SheetDialog);
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_sort_by_options_layout, null);
        mBottomSheetDialog.setContentView(sheetView);

        CardView ascending = (CardView) mBottomSheetDialog.findViewById(R.id.sort_by_ascending_bottom_sheet);
        CardView descending = (CardView) mBottomSheetDialog.findViewById(R.id.sort_by_descending_bottom_sheet);

        mBottomSheetDialog.show();

        ascending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBy("Ascending");
                mBottomSheetDialog.dismiss();
            }
        });

        descending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBy("Descending");
                mBottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main_options_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_user_options:
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.SheetDialog);
                View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_album_main_options_layout, null);
                mBottomSheetDialog.setContentView(sheetView);

                CardView add = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_add_album_bottom_sheet);
                CardView sortBy = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_sort_bottom_sheet);
                CardView viewSize = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_thumbnail_bottom_sheet);
                CardView cancel = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_options_cancel_bottom_sheet);
                TextView textView = (TextView) viewSize.findViewById(R.id.album_action_main_thumbnail_text_bottom_sheet);

                privacySetting = preferences.getString("view_size", "Large");
                textView.setText(privacySetting.equals("Large") ? "Small Thumbnails" : "Large Thumbnails");

                mBottomSheetDialog.show();

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("demo", "Clicked");
                        final AlertDialog builder = new AlertDialog.Builder(getActivity()).create();
                        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.text_input_add_album, null);
                        final EditText input = (EditText) viewInflated.findViewById(R.id.album_input);

                        builder.setView(viewInflated);
                        builder.setCancelable(true);

                        Button okButton = (Button) viewInflated.findViewById(R.id.builder_yes_button);
                        Button noButton = (Button) viewInflated.findViewById(R.id.builder_no_button);

                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String isPublicString = preferences.getString("current_privacy", "Public");
                                boolean isPublic = (isPublicString.equals("Public"));

                                firebaseCommands.createPhotoCollection(input.getText().toString(), isPublic);

                                Bundle bundle = new Bundle();
                                bundle.putString(ALBUM_NAME, input.getText().toString());
                                UserImageFragment userImageFragment = new UserImageFragment();
                                userImageFragment.setArguments(bundle);
                                ((MainActivity) getActivity()).getSupportFragmentManager()
                                        .beginTransaction()
                                        .addToBackStack(USER_IMAGE_FRAGMENT_TAG)
                                        .replace(R.id.fragment_container, userImageFragment, USER_IMAGE_FRAGMENT_TAG)
                                        .commit();

                                builder.dismiss();
                            }
                        });
                        noButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                builder.dismiss();
                            }
                        });
                        mBottomSheetDialog.dismiss();
                        builder.show();
                    }
                });

                sortBy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createSortByBottomSheet();
                        mBottomSheetDialog.dismiss();
                    }
                });

                viewSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (privacySetting.equals("Large")) {
                            preferences.edit().putString("view_size", "Small").apply();
                            Toast.makeText(getContext(), "Small Thumbnails", Toast.LENGTH_SHORT).show();
                            updateUI();
                        } else {
                            preferences.edit().putString("view_size", "Large").apply();
                            Toast.makeText(getContext(), "Large Thumbnails", Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                        mBottomSheetDialog.dismiss();
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
        return super.onOptionsItemSelected(item);
    }
}
