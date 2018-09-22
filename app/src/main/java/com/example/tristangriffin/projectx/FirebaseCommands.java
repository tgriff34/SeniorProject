package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class FirebaseCommands {

    private static final FirebaseCommands ourInstance = new FirebaseCommands();

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseUser user = firebaseAuth.getCurrentUser();

    private ArrayList<String> allImages;
    OnGetDataListener ml;

    public static FirebaseCommands getInstance() {
        return ourInstance;
    }

    private FirebaseCommands() {

    }

    public void createUser(String email, String password, Activity activity, final OnSignUpListener listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "createUserWithEmailAndPassword:success");
                            user = firebaseAuth.getCurrentUser();
                            createNewUserCollection(user, listener);
                        } else {
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Log.e("Firebase", "createUserWithEmailAndPassword:failure");
                            Log.e("Firebase", "Failed Registration", e);
                        }
                    }
                });
    }

    public void signIn(String email, String password, Activity activity, final OnSignInListener listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = firebaseAuth.getCurrentUser();
                            listener.onSignIn(user);
                            Log.d("demo", "signedInUserWithEmailAndPassword:success");
                        } else {
                            Log.d("demo", "signedInUserWithEmailAndPassword:failure");
                        }
                    }
                });
    }

    private void createNewUserCollection(FirebaseUser user, final OnSignUpListener listener) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("id", user.getUid());
        newUser.put("email", user.getEmail());
        db.collection("users")
                .document(user.getUid())
                .set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "addCollectionOnUserCreation:success");
                        listener.onSignUp();
                    }
                });
    }

    public void uploadPhotos(Uri uri, final String longitude, final String latitude,
                             final String timeCreated, final String dateCreated) {
        final StorageReference fileRef = storageReference.child("images/public" + user.getUid() + "/" + uri.getLastPathSegment());
        final String TAG = uri.getLastPathSegment();

        UploadTask uploadTask = fileRef.putFile(uri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Firebase", "uploadSuccess:true");
                addToDatabase(fileRef, TAG, longitude, latitude, timeCreated, dateCreated);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "uploadSuccess:failure");
            }
        });
    }

    private void addToDatabase(StorageReference ref, final String TAG, final String longitude,
                               final String latitude, final String timeCreated, final String dateCreated) {
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Map<String, Object> dbImageReference = new HashMap<>();
                dbImageReference.put("ref", uri.toString());
                dbImageReference.put("longitude", longitude);
                dbImageReference.put("latitude", latitude);
                dbImageReference.put("time", timeCreated);
                dbImageReference.put("date", dateCreated);
                db.collection("users")
                        .document(user.getUid())
                        .collection("images")
                        .document(TAG)
                        .set(dbImageReference)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Firebase", "dbRef:success");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "debRef:failure");
                    }
                });
            }
        });
    }

    private void deleteFromDatabase(String TAG) {
        db.collection("users")
                .document(user.getUid())
                .collection("images")
                .document(TAG)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "deleteFromDatabase:success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "deleteFromDatabase:failure");
                    }
                });
    }

    public void getPhotos(final OnGetDataListener listener) {
        allImages = new ArrayList<>();
        db.collection("users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("images")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                allImages.add(document.getString("ref"));
                            }
                            Log.d("demo", "Cloud images: " + allImages.toString());
                            listener.onSuccess(allImages);
                        }
                    }
                });
    }
}
