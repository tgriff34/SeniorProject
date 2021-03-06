package com.example.tristangriffin.projectx.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tristangriffin.projectx.Activities.MainActivity;
import com.example.tristangriffin.projectx.Adapters.RecyclerViewCompactListAdapter;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.example.tristangriffin.projectx.Listeners.OnGetAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Adapters.RecyclerViewListAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.example.tristangriffin.projectx.Activities.MainActivity.ALBUM_NAME;
import static com.example.tristangriffin.projectx.Activities.MainActivity.USER_IMAGE_FRAGMENT_TAG;

public class UserFragment extends Fragment {

    private ArrayList<Album> albums;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private TextView textView;

    private Context context;
    private Activity activity;

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private SharedPreferences preferences;
    private String privacySetting;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = getActivity();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        //getActivity().setTitle(R.string.album_name);
        //final Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        TextView toolbarTextView = activity.findViewById(R.id.toolbar_title);
        toolbarTextView.setText(R.string.album_name);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recyclerView = view.findViewById(R.id.recycler_view);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        progressBar = view.findViewById(R.id.user_progressbar);
        textView = view.findViewById(R.id.user_textInfo);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);
        textView.setVisibility(View.GONE);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.recycler_view_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

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
            public void onGetAlbumSuccess(ArrayList<Album> listOfAlbums) {
                if (!listOfAlbums.isEmpty()) {
                    for (int i = 0; i < listOfAlbums.size(); i++) {
                        albums.add(listOfAlbums.get(i));
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
        firebaseCommands.getThumbnail(albums.get(position), new OnGetThumbnailListener() {
            @Override
            public void onGetThumbnailSuccess(String string) {
                if (string != null) {
                    albums.get(position).setThumbnail(string);
                } else {
                    albums.get(position).setThumbnail(null);
                }
                updateUI();
            }
        });
    }

    private void updateUI() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String currentView = preferences.getString("view_size", "Large");
        if (currentView.equals("Large")) {
            recyclerView.setAdapter(new RecyclerViewListAdapter(activity, albums));
        } else {
            recyclerView.setAdapter(new RecyclerViewCompactListAdapter(activity, albums));
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
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(context, R.style.SheetDialog);
        View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_sort_by_options_layout, null);
        mBottomSheetDialog.setContentView(sheetView);

        CardView ascending = (CardView) mBottomSheetDialog.findViewById(R.id.sort_by_ascending_bottom_sheet);
        CardView descending = (CardView) mBottomSheetDialog.findViewById(R.id.sort_by_descending_bottom_sheet);
        CardView cancel = (CardView) mBottomSheetDialog.findViewById(R.id.sort_by_cancel_bottom_sheet);

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

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.dismiss();
            }
        });
    }

    private boolean validate(EditText editText) {
        boolean isValid = true;
        if (TextUtils.isEmpty(editText.getText().toString())) {
            isValid = false;
            editText.setError("Cannot be empty.");
        } else {
            editText.setError(null);
        }
        return isValid;
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
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(context, R.style.SheetDialog);
                View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_album_main_options_layout, null);
                mBottomSheetDialog.setContentView(sheetView);

                CardView add = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_add_album_bottom_sheet);
                CardView sortBy = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_sort_bottom_sheet);
                CardView viewSize = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_thumbnail_bottom_sheet);
                CardView cancel = (CardView) mBottomSheetDialog.findViewById(R.id.album_action_main_options_cancel_bottom_sheet);
                TextView textView = (TextView) viewSize.findViewById(R.id.album_action_main_thumbnail_text_bottom_sheet);
                ImageView imageView = (ImageView) viewSize.findViewById(R.id.album_action_main_thumbnail_image_bottom_sheet);

                privacySetting = preferences.getString("view_size", "Large");
                textView.setText(privacySetting.equals("Large") ? "Small Thumbnails" : "Large Thumbnails");
                imageView.setImageResource(privacySetting.equals("Large") ? R.drawable.ic_list: R.drawable.ic_picture);

                mBottomSheetDialog.show();

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("demo", "Clicked");
                        final AlertDialog builder = new AlertDialog.Builder(activity).create();
                        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.text_input_add_album, null);
                        final EditText input = (EditText) viewInflated.findViewById(R.id.album_input);

                        builder.setView(viewInflated);
                        builder.setCancelable(true);

                        Button okButton = (Button) viewInflated.findViewById(R.id.builder_yes_button);
                        Button noButton = (Button) viewInflated.findViewById(R.id.builder_no_button);

                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (validate(input)) {
                                    String isPublicString = preferences.getString("current_privacy", "Public");
                                    boolean isPublic = (isPublicString.equals("Public"));

                                    Album newAlbum = new Album();
                                    newAlbum.setName(input.getText().toString());
                                    newAlbum.setFavorite(false);
                                    newAlbum.setId(firebaseCommands.user.getUid());
                                    newAlbum.setPublic(isPublic);
                                    newAlbum.setDate(Calendar.getInstance().getTime());

                                    firebaseCommands.createPhotoCollection(newAlbum);

                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable(ALBUM_NAME, new Gson().toJson(newAlbum));
                                    UserImageFragment userImageFragment = new UserImageFragment();
                                    userImageFragment.setArguments(bundle);
                                    ((MainActivity) activity).setFragmentNoTransition(userImageFragment, USER_IMAGE_FRAGMENT_TAG);

                                    builder.dismiss();
                                }
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
                            updateUI();
                        } else {
                            preferences.edit().putString("view_size", "Large").apply();
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
