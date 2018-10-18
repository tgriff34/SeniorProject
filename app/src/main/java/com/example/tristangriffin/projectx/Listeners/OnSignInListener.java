package com.example.tristangriffin.projectx.Listeners;

import com.google.firebase.auth.FirebaseUser;

public interface OnSignInListener {
    void onSignIn(FirebaseUser user);
    void failedSignIn();
}
