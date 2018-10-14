package com.example.tristangriffin.projectx;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FirebaseCommands {

    private static final FirebaseCommands ourInstance = new FirebaseCommands();

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseUser user = firebaseAuth.getCurrentUser();

    private LinkedHashMap<String, String> allImages;
    private ArrayList<String> allAlbums;
    private String thumbnailRef;

    private static final String USER_TAG = "users";

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

    public void signIn(String email, String password, final Activity activity, final OnSignInListener listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = firebaseAuth.getCurrentUser();
                            listener.onSignIn(user);
                            Log.d("demo", "signedInUserWithEmailAndPassword:success");
                        } else {
                            listener.failedSignIn();
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

    public void uploadPhotos(Uri uri, final String collection, final String location, final String longitude, final String latitude,
                             final String timeCreated, final String dateCreated) {
        final StorageReference fileRef = storageReference.child("images/public/" + user.getUid() + "/" + uri.getLastPathSegment());
        final String TAG = uri.getLastPathSegment();

        UploadTask uploadTask = fileRef.putFile(uri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Firebase", "uploadSuccess:true");
                addToDatabase(fileRef, null, collection, TAG, location, longitude, latitude, timeCreated, dateCreated);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Firebase", "uploadSuccess:failure");
            }
        });
    }

    private void addToDatabase(@Nullable StorageReference ref, @Nullable final String URI, final String collection,
                               final String TAG, final String location, final String longitude, final String latitude,
                               final String timeCreated, final String dateCreated) {
        final Map<String, Object> dbImageReference = new HashMap<>();
        if (URI == null) {
            //First time adding to database
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    dbImageReference.put("ref", uri.toString());
                    dbImageReference.put("longitude", longitude);
                    dbImageReference.put("latitude", latitude);
                    dbImageReference.put("location", location);
                    dbImageReference.put("time", timeCreated);
                    dbImageReference.put("date", dateCreated);
                    db.collection("users")
                            .document(user.getUid())
                            .collection("public")
                            .document(collection)
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
    }

    public void deleteFromDatabase(final String TAG, final String collection, final String setting) {
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(collection)
                .collection("images")
                .document(TAG)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "deleteFromDatabase:success");
                        Log.d("Firebase", TAG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "deleteFromDatabase:failure");
                    }
                });
    }

    public void deletePhotoCollection(final String name, final String setting, final OnDeleteAlbumListener listener) {
        getPhotos(name, setting, new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(LinkedHashMap<String, String> images) {
                for (Map.Entry<String, String> photo : images.entrySet()) {
                    deleteFromDatabase(photo.getKey(), name, setting);
                }
                db.collection("users")
                        .document(user.getUid())
                        .collection(setting)
                        .document(name)
                        .delete();
                listener.onDeleteAlbum(true);
            }
        });
    }

    public void favoritePhotoCollection(final String name, final String setting) {
        Map<String, Object> isFavorite = new HashMap<>();
        isFavorite.put("name", name);
        isFavorite.put("isFavorite", true);
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(name)
                .set(isFavorite);

        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(name)
                .collection("images")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> albumName = new HashMap<>();
                            albumName.put("name", name);
                            db.collection("users")
                                    .document(user.getUid())
                                    .collection("favorites")
                                    .document(name).set(albumName);
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                db.collection("users")
                                        .document(user.getUid())
                                        .collection("favorites")
                                        .document(name)
                                        .collection("images")
                                        .document(documentSnapshot.getId())
                                        .set(documentSnapshot.getData());
                                Log.d("demo", documentSnapshot.getId() + " => " + documentSnapshot.getData());
                            }
                        }
                    }
                });
    }

    public void unfavoritePhotoCollection(final String name) {
        Map<String, Object> albumSetting = new HashMap<>();
        albumSetting.put("name", name);
        albumSetting.put("isFavorite", false);
        db.collection("users")
                .document(user.getUid())
                .collection("public")
                .document(name)
                .set(albumSetting);

        /**
         * TODO: Might need to add a listener here...
         */
        deletePhotoCollection(name, "favorites", new OnDeleteAlbumListener() {
            @Override
            public void onDeleteAlbum(boolean isDeleted) {
            }
        });
    }

    public void isFavoritePhotoCollection(final String name, final String setting, final OnGetIsFavoriteAlbumListener listener) {
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean isFavorite = (boolean) task.getResult().get("isFavorite");
                            listener.isFavoriteAlbum(isFavorite);
                        }
                    }
                });
    }

    public void createPhotoCollection(String name, String setting) {
        Map<String, Object> collectionName = new HashMap<>();
        collectionName.put("name", name);
        collectionName.put("isFavorite", false);
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(name)
                .set(collectionName);
    }

    public void getAlbums(final String setting, final OnGetAlbumListener listener) {
        allAlbums = new ArrayList<>();
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                allAlbums.add(documentSnapshot.get("name").toString());
                            }
                            listener.onGetAlbumSuccess(allAlbums);
                        } else {
                            Log.d("Firebase", "Error getting albums", task.getException());
                        }
                    }
                });
    }

    public void getPhotos(final String albumName, final String setting, final OnGetPhotosListener listener) {
        allImages = new LinkedHashMap<>();
        //TODO: Get images from album
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(albumName)
                .collection("images")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                allImages.put(document.getId(), document.getString("ref"));
                            }
                            //Log.d("demo", "Cloud images: " + allImages.toString());
                            listener.onGetPhotosSuccess(allImages);
                        }
                    }
                });
    }

    public void getThumbnail(final String albumName, final String setting, final OnGetThumbnailListener listener) {
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(albumName)
                .collection("images")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                thumbnailRef = documentSnapshot.getString("ref");
                            }
                            listener.onGetThumbnailSuccess(thumbnailRef);
                        }
                    }
                });
    }
}
