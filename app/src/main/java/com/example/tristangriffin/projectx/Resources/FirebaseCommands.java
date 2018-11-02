package com.example.tristangriffin.projectx.Resources;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.tristangriffin.projectx.Listeners.OnDeleteAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetFavoritedAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetIfFavoritedAlbumListener;
import com.example.tristangriffin.projectx.Listeners.OnGetPhotosListener;
import com.example.tristangriffin.projectx.Listeners.OnGetPicLatLongListener;
import com.example.tristangriffin.projectx.Listeners.OnGetSearchAlbumsListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.Listeners.OnSignInListener;
import com.example.tristangriffin.projectx.Listeners.OnSignUpListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
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
                    dbImageReference.put("album", collection);
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
    }

    public void unfavoritePhotoCollection(final String name, final String setting) {
        Map<String, Object> albumSetting = new HashMap<>();
        albumSetting.put("name", name);
        albumSetting.put("isFavorite", false);
        db.collection("users")
                .document(user.getUid())
                .collection(setting)
                .document(name)
                .set(albumSetting);
    }

    public void getIfFavoritedPhotoCollection(String albumName, final OnGetIfFavoritedAlbumListener listener) {
        db.collection("users")
                .document(user.getUid())
                .collection("public")
                .document(albumName)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    boolean isFavorite = documentSnapshot.getBoolean("isFavorite");
                    listener.getIfFavoritedAlbumListener(isFavorite);
                }
            }
        });
    }

    public void getFavoritedPhotoCollection(final OnGetFavoritedAlbumListener listener) {
        final ArrayList<String> favoritedAlbums = new ArrayList<>();
        db.collection("users")
                .document(user.getUid())
                .collection("public")
                .whereEqualTo("isFavorite", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                favoritedAlbums.add(documentSnapshot.getId());
                            }
                            listener.getFavoritedAlbum(favoritedAlbums);
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

    public void getPictureLatLong(final String album, final OnGetPicLatLongListener listener) {

        final Map<String[], double[]> picInfoMap = new HashMap<>();

        getAlbums("public", new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<String> albums) {
                db.collection("users")
                        .document(user.getUid())
                        .collection("public")
                        .document(album)
                        .collection("images")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                                        String location = documentSnapshot.getId();
                                        String ref = documentSnapshot.getString("ref");
                                        double latitude = Double.parseDouble(documentSnapshot.getString("latitude"));
                                        double longitude = Double.parseDouble(documentSnapshot.getString("longitude"));
                                        double[] latLong = new double[]{latitude, longitude};
                                        String[] strings = new String[]{location, ref};

                                        picInfoMap.put(strings, latLong);

                                    }
                                    Log.d("demo", picInfoMap.toString());
                                    listener.getPicLatLong(picInfoMap);
                                }
                            }
                        });
            }
        });
    }

    public void searchAlbums(final String searchString, final OnGetSearchAlbumsListener listener) {
        final ArrayList<String> searchedAlbums = new ArrayList<>();

        CollectionReference publicRef = db.collection("users").document(user.getUid()).collection("public");
        CollectionReference privateRef = db.collection("users").document(user.getUid()).collection("private");

        publicRef.orderBy("name").startAt(searchString).endAt(searchString + "\uf8ff").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        searchedAlbums.add(documentSnapshot.getId());
                    }
                }
            }
        });
        privateRef.orderBy("name").startAt(searchString).endAt(searchString + "\uf8ff").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        searchedAlbums.add(documentSnapshot.getId());
                    }
                }
            }
        });
        getAlbums("public", new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<String> albums) {
                for (String album : albums) {
                    db.collection("users")
                            .document(user.getUid())
                            .collection("public")
                            .document(album)
                            .collection("images")
                            .orderBy("location").startAt(searchString).endAt(searchString + "\uf8ff").get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                            searchedAlbums.add(documentSnapshot.get("album").toString());
                                        }
                                    }
                                }
                            });
                }
                listener.searchedAlbums(searchedAlbums);
            }
        });
    }
}
