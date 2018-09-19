package com.example.tristangriffin.projectx;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public interface OnGetDataListener {
    void onSuccess(ArrayList<String> images);
}
