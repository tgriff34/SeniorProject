package com.example.tristangriffin.projectx;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public interface OnGetPhotosListener {
    void onGetPhotosSuccess(LinkedHashMap<String, String> images);
}
