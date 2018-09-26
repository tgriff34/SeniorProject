package com.example.tristangriffin.projectx;

import com.google.firebase.auth.FirebaseUser;

public interface OnSignInListener {
    void onSignIn(FirebaseUser user);
    void failedSignIn();
}
