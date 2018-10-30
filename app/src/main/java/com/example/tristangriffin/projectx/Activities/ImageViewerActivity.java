package com.example.tristangriffin.projectx.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.example.tristangriffin.projectx.Listeners.OnGetPhotosListener;
import com.example.tristangriffin.projectx.R;
import com.example.tristangriffin.projectx.Resources.FirebaseCommands;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.example.tristangriffin.projectx.Fragments.UserImageFragment.ALBUM_SELECT_NAME;
import static com.example.tristangriffin.projectx.Fragments.UserImageFragment.PICTURE_SELECT_NAME;

public class ImageViewerActivity extends AppCompatActivity {

    private FirebaseCommands firebaseCommands = FirebaseCommands.getInstance();

    private LinkedHashMap<String, String> cloudImages;
    private String currentImage;
    private String albumName;
    private ProgressBar progressBar;
    private ImageSwitcher imageView;
    private Button cancelButton;
    private int currentPosition;

    private Animation mSlideInLeft;
    private Animation mSlideOutRight;
    private Animation mSlideInRight;
    private Animation mSlideOutLeft;

    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.imageViewer_imageView);
        cancelButton = findViewById(R.id.imageViewer_exit);
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
            albumName = bundle.getString(ALBUM_SELECT_NAME);
            currentImage = bundle.getString(PICTURE_SELECT_NAME);
        }

        getImages();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
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

    private void getImages() {
        firebaseCommands.getPhotos(albumName, "public", new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(LinkedHashMap<String, String> images) {
                cloudImages = images;
                progressBar.setVisibility(View.GONE);
                currentPosition = new ArrayList<>(cloudImages.keySet()).indexOf(currentImage);
                //updateUI();
                Glide.with(ImageViewerActivity.this).load(new ArrayList<>(cloudImages.values()).get(currentPosition)).into((ImageView) imageView.getCurrentView());
            }
        });
    }

    private void updateUI() {
        Log.d("demo", "Current Position: " + currentPosition);
    }

    private void moveNextOrPrevious(int delta) {
        int nextImagePosition = currentPosition + delta;

        if (nextImagePosition < 0) {
            return;
        } else if (nextImagePosition > cloudImages.size()) {
            return;
        }

        imageView.setInAnimation(delta > 0 ? mSlideInRight : mSlideInLeft);
        imageView.setOutAnimation(delta > 0 ? mSlideOutLeft : mSlideOutRight);

        currentPosition = nextImagePosition;

        Log.d("demo", "Current Position: " + currentPosition);

        //updateUI();
        Glide.with(this).load(new ArrayList<>(cloudImages.values()).get(currentPosition)).into((ImageView) imageView.getCurrentView());
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
