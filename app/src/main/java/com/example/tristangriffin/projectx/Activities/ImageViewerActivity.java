package com.example.tristangriffin.projectx.Activities;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.tristangriffin.projectx.Listeners.OnGetIfFavoritedAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetPhotosListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Models.Image;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.example.tristangriffin.projectx.Activities.MainActivity.ALBUM_SELECT_NAME;
import static com.example.tristangriffin.projectx.Activities.MainActivity.PICTURE_SELECT_NAME;

public class ImageViewerActivity extends AppCompatActivity {

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private ArrayList<Image> cloudImages;
    private String currentImage;
    private String myAlbum;
    private Album album;
    private boolean checkIfIsFavorite;

    private ProgressBar progressBar;
    private ImageSwitcher imageView;
    private ImageView cancelButton, favoriteButton;
    private TextView imageText;

    private int currentPosition;

    private Animation mSlideInLeft;
    private Animation mSlideOutRight;
    private Animation mSlideInRight;
    private Animation mSlideOutLeft;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentTheme = preferences.getString("current_theme", "Light");
        if (currentTheme.equals("Light")) {
            setTheme(R.style.LightAppTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.imageViewer_imageView);
        imageText = findViewById(R.id.imageViewer_text);
        cancelButton = findViewById(R.id.imageViewer_exit);
        favoriteButton = findViewById(R.id.imageViewer_favorite);
        progressBar = findViewById(R.id.imageViewer_progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        //Animations
        mSlideInLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        mSlideOutRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        mSlideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        mSlideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        Bundle bundle = this.getIntent().getExtras();

        if (bundle != null) {
            myAlbum = bundle.getString(ALBUM_SELECT_NAME);
            currentImage = bundle.getString(PICTURE_SELECT_NAME);
        }

        album = new Gson().fromJson(myAlbum, Album.class);

        getImages();

        firebaseCommands.getIfFavoritedPhotoCollection(album.getName(), new OnGetIfFavoritedAlbumListener() {
            @Override
            public void getIfFavoritedAlbumListener(boolean isFavorite) {
                if (isFavorite) {
                    checkIfIsFavorite = true;
                    favoriteButton.setImageResource(R.drawable.ic_heart_closed_white);
                } else {
                    checkIfIsFavorite = false;
                    favoriteButton.setImageResource(R.drawable.ic_heart_white);
                }
            }
        });


        //Button presses
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIfIsFavorite) {
                    checkIfIsFavorite = false;
                    //firebaseCommands.favoritePhotoCollection(new Album().setName(albumName));
                    favoriteButton.setImageResource(R.drawable.ic_heart_white);
                } else {
                    checkIfIsFavorite = true;
                    //firebaseCommands.favoritePhotoCollection(albumName);
                    favoriteButton.setImageResource(R.drawable.ic_heart_closed_white);
                }
            }
        });

        //Image Switcher
        imageView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView view = new ImageView(ImageViewerActivity.this);
                view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                view.setLayoutParams(new ImageSwitcher.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                return view;
            }
        });

        //Detect gestures
        gestureDetector = new GestureDetector(this, new SwipeListener());

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    //Private Funcs
    private void getImages() {
        firebaseCommands.getPhotos(album, new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(ArrayList<Image> images) {
                cloudImages = images;
                for (int i = 0; i < cloudImages.size(); i++) {
                    if (cloudImages.get(i).getId().equals(currentImage)) {
                        currentPosition = i;
                    }
                }
                updateUI();
            }
        });
    }

    private void updateUI() {
        Log.d("demo", "Current Position: " + currentPosition);
        Glide.with(this)
                .load(cloudImages.get(currentPosition).getRef())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (currentPosition == cloudImages.size()) {
                            currentPosition = 0;
                        }
                        imageView.setImageDrawable(resource);
                        return true;
                    }
                }).into((ImageView) imageView.getCurrentView());

        imageText.setText(cloudImages.get(currentPosition).getLocation());

        progressBar.setVisibility(View.GONE);
    }

    private void moveNextOrPrevious(int delta) {
        int nextImagePosition = currentPosition + delta;

        if (nextImagePosition < 0) {
            Log.d("demo", "Next Position: " + nextImagePosition + "  " + cloudImages.size());
            return;
        } else if (nextImagePosition >= cloudImages.size()) {
            Log.d("demo", "Next Position: " + nextImagePosition + "  " + cloudImages.size());
            return;
        }

        imageView.setInAnimation(delta > 0 ? mSlideInRight : mSlideInLeft);
        imageView.setOutAnimation(delta > 0 ? mSlideOutLeft : mSlideOutRight);

        currentPosition = nextImagePosition;

        Log.d("demo", "Current Position: " + currentPosition);

        updateUI();
    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 75;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    moveNextOrPrevious(1);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    moveNextOrPrevious(-1);
                }
            } catch (Exception e) {

            }
            return false;
        }
    }
}
