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
import com.example.tristangriffin.projectx.Listeners.OnGetPublicAlbumsListener;
import com.example.tristangriffin.projectx.Listeners.OnGetSearchAlbumsListener;
import com.example.tristangriffin.projectx.Listeners.OnGetThumbnailListener;
import com.example.tristangriffin.projectx.Listeners.OnGetUsersListener;
import com.example.tristangriffin.projectx.Listeners.OnSignInListener;
import com.example.tristangriffin.projectx.Listeners.OnSignUpListener;
import com.example.tristangriffin.projectx.Models.Album;
import com.example.tristangriffin.projectx.Models.Image;
import com.example.tristangriffin.projectx.Models.User;
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
import java.util.Map;

public class FirebaseCommands {

    private static final FirebaseCommands ourInstance = new FirebaseCommands();

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseUser user = firebaseAuth.getCurrentUser();

    private ArrayList<Image> allImages;
    private ArrayList<User> users;
    private ArrayList<String> allAlbums;

    private static final String USER_TAG = "users";
    private static final String ALBUM_TAG = "albums";
    private static final String IMAGES_TAG = "images";

    public static FirebaseCommands getInstance() {
        return ourInstance;
    }

    private FirebaseCommands() {

    }

    /**
     *  USER METHODS
     */
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

    public void signOut() {
        user = null;
        firebaseAuth.signOut();
    }

    private void getUsers(final OnGetUsersListener listener) {
        users = new ArrayList<>();
        db.collection(USER_TAG).
                get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        User user = new User();
                        user.setId(queryDocumentSnapshot.getId());
                        users.add(user);
                    }
                    listener.onGetUsers(users);
                }
            }
        });
    }

    private void getPublicAlbums(final OnGetPublicAlbumsListener listener) {
        final ArrayList<Album> publicAlbums = new ArrayList<>();

        getUsers(new OnGetUsersListener() {
            @Override
            public void onGetUsers(ArrayList<User> users) {
                for (User user: users) {
                    if (user.getId() != firebaseAuth.getUid()) {
                        db.collection(USER_TAG)
                                .document(user.getId())
                                .collection(ALBUM_TAG)
                                .whereEqualTo("isPublic", true)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                Album album = new Album();
                                                album.setName(documentSnapshot.getId());
                                                album.setId(documentSnapshot.getString("id"));
                                                album.setFavorite(documentSnapshot.getBoolean("isFavorite"));
                                                album.setPublic(documentSnapshot.getBoolean("isPublic"));

                                                publicAlbums.add(album);
                                            }
                                            listener.getPublicAlbums(publicAlbums);
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    /**
     *  DIVIDER
     */
    private void createNewUserCollection(FirebaseUser user, final OnSignUpListener listener) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("id", user.getUid());
        newUser.put("email", user.getEmail());
        db.collection(USER_TAG)
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
                    db.collection(USER_TAG)
                            .document(user.getUid())
                            .collection(ALBUM_TAG)
                            .document(collection)
                            .collection(IMAGES_TAG)
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

    public void deleteFromDatabase(final String TAG, final String collection) {
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
                .document(collection)
                .collection(IMAGES_TAG)
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

    public void deletePhotoCollection(final String name, final OnDeleteAlbumListener listener) {
        getPhotos(name, new OnGetPhotosListener() {
            @Override
            public void onGetPhotosSuccess(ArrayList<Image> images) {
                for (Image image: images) {
                    deleteFromDatabase(image.getId(), name);
                }
                db.collection("users")
                        .document(user.getUid())
                        .collection(ALBUM_TAG)
                        .document(name)
                        .delete();
                listener.onDeleteAlbum(true);
            }
        });
    }

    /* PASS ENTIRE ALBUM */
    public void favoritePhotoCollection(Album album) {
        Log.d("demo", "Favoriting");
        Map<String, Object> favorite = new HashMap<>();
        favorite.put("name", album.getName());
        if (album.isFavorite()) {
            favorite.put("isFavorite", false);
        } else {
            favorite.put("isFavorite", true);
        }
        favorite.put("isPublic", album.isPublic());
        favorite.put("id", user.getUid());
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
                .document(album.getName())
                .set(favorite);
    }

    public void getIfFavoritedPhotoCollection(String albumName, final OnGetIfFavoritedAlbumListener listener) {
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
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
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
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

    public void createPhotoCollection(String name, boolean setting) {
        Map<String, Object> collectionName = new HashMap<>();
        collectionName.put("name", name);
        collectionName.put("isFavorite", false);
        collectionName.put("isPublic", setting);
        collectionName.put("id", user.getUid());
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
                .document(name)
                .set(collectionName);
    }

    public void getAlbums(final OnGetAlbumListener listener) {
        allAlbums = new ArrayList<>();
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                allAlbums.add(documentSnapshot.getString("name"));
                            }
                            listener.onGetAlbumSuccess(allAlbums);
                        } else {
                            Log.d("Firebase", "Error getting albums", task.getException());
                        }
                    }
                });
    }

    public void getPhotos(final String albumName, final OnGetPhotosListener listener) {
        allImages = new ArrayList<>();
        //TODO: Get images from album
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
                .document(albumName)
                .collection(IMAGES_TAG)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Image image = new Image();
                                image.setId(document.getId());
                                image.setRef(document.getString("ref"));
                                image.setAlbum(document.getString("album"));
                                image.setLocation(document.getString("location"));
                                image.setLatitude(document.getString("latitude"));
                                image.setLongitude(document.getString("longitude"));
                                allImages.add(image);
                                //allImages.put(document.getId(), document.getString("ref"));
                            }
                            //Log.d("demo", "Cloud images: " + allImages.toString());
                            listener.onGetPhotosSuccess(allImages);
                        }
                    }
                });
    }

    public void getThumbnail(final String albumName, final OnGetThumbnailListener listener) {
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
                .document(albumName)
                .collection(IMAGES_TAG)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String thumbnailRef = null;
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                                thumbnailRef = documentSnapshot.getString("ref");
                            }
                            listener.onGetThumbnailSuccess(thumbnailRef);
                        } else {
                            listener.onGetThumbnailSuccess(null);
                        }
                    }
                });
    }

    public void searchAlbums(final String searchString, final OnGetSearchAlbumsListener listener) {
        final ArrayList<String> searchedAlbums = new ArrayList<>();

        CollectionReference userRef = db.collection(USER_TAG).document(user.getUid()).collection(ALBUM_TAG);

        userRef.orderBy("name").startAt(searchString).endAt(searchString + "\uf8ff").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        searchedAlbums.add(documentSnapshot.getId());
                    }
                }
            }
        });

        getPublicAlbums(new OnGetPublicAlbumsListener() {
            @Override
            public void getPublicAlbums(ArrayList<Album> albums) {
                for (Album album : albums) {
                    if (!user.getUid().equals(album.getId())) {
                        db.collection(USER_TAG)
                                .document(album.getId())
                                .collection(ALBUM_TAG)
                                .orderBy("name").startAt(searchString).endAt(searchString + "\uf8ff").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                        searchedAlbums.add(queryDocumentSnapshot.getId());
                                    }
                                }
                            }
                        });

                        db.collection(USER_TAG)
                                .document(album.getId())
                                .collection(ALBUM_TAG)
                                .document(album.getName())
                                .collection(IMAGES_TAG)
                                .orderBy("location").startAt(searchString).endAt(searchString + "\uf8ff").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                searchedAlbums.add(documentSnapshot.getString("album"));
                                            }
                                        }
                                    }
                                });
                    }
                }
                listener.searchedAlbums(searchedAlbums);
            }
        });

        getAlbums(new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<String> albums) {
                for (String album : albums) {
                    db.collection(USER_TAG)
                            .document(user.getUid())
                            .collection(ALBUM_TAG)
                            .document(album)
                            .collection(IMAGES_TAG)
                            .orderBy("location").startAt(searchString).endAt(searchString + "\uf8ff").get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                            searchedAlbums.add(documentSnapshot.getString("album"));
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
