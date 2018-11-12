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
import java.util.Calendar;
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
    private ArrayList<Album> allAlbums;
    private ArrayList<Album> favoriteAlbums;

    private static final String USER_TAG = "users";
    private static final String ALBUM_TAG = "albums";
    private static final String IMAGES_TAG = "images";

    public static FirebaseCommands getInstance() {
        return ourInstance;
    }

    private FirebaseCommands() {

    }

    /**
     * USER METHODS
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

    public void getUsers(final OnGetUsersListener listener) {
        users = new ArrayList<>();
        db.collection(USER_TAG).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

    private ArrayList<Album> publicUserAlbum;
    private void getPublicAlbums(User user, final OnGetPublicAlbumsListener listener) {
        publicUserAlbum = new ArrayList<>();
        db.collection(USER_TAG)
                .document(user.getId())
                .collection(ALBUM_TAG)
                .whereEqualTo("isPublic", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Album getAlbum = new Album();
                                getAlbum.setName(documentSnapshot.getString("name"));
                                getAlbum.setFavorite(documentSnapshot.getBoolean("isFavorite"));
                                getAlbum.setPublic(documentSnapshot.getBoolean("isPublic"));
                                getAlbum.setId(documentSnapshot.getString("id"));
                                getAlbum.setDate(documentSnapshot.getDate("date"));

                                publicUserAlbum.add(getAlbum);
                            }
                            listener.getPublicAlbums(publicUserAlbum);
                        }

                    }
                });
    }

    /**
     * DIVIDER
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

    // ******************************************//
    //               Adding Photos               //
    // ******************************************//
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

    //******************************************//
    //                Deleting                  //
    //******************************************//
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
                for (Image image : images) {
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

    // *******************************************//
    //               Favorite Album               //
    // *******************************************//
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
        favorite.put("id", album.getId());
        favorite.put("date", album.getDate());
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
        favoriteAlbums = new ArrayList<>();
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
                                Album getAlbum = new Album();
                                getAlbum.setName(documentSnapshot.getString("name"));
                                getAlbum.setFavorite(documentSnapshot.getBoolean("isFavorite"));
                                getAlbum.setPublic(documentSnapshot.getBoolean("isPublic"));
                                getAlbum.setId(documentSnapshot.getString("id"));
                                getAlbum.setDate(documentSnapshot.getDate("date"));

                                favoriteAlbums.add(getAlbum);
                            }
                            listener.getFavoritedAlbum(favoriteAlbums);
                        }
                    }
                });
    }

    // *******************************************//
    //               Create new album             //
    // *******************************************//
    public void createPhotoCollection(Album album) {
        Map<String, Object> collectionName = new HashMap<>();
        collectionName.put("name", album.getName());
        collectionName.put("isFavorite", album.isFavorite());
        collectionName.put("isPublic", album.isPublic());
        collectionName.put("id", album.getId());
        collectionName.put("date", album.getDate());
        db.collection(USER_TAG)
                .document(user.getUid())
                .collection(ALBUM_TAG)
                .document(album.getName())
                .set(collectionName);
    }

    // *******************************************//
    //                 get album                  //
    // *******************************************//
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
                                Album getAlbum = new Album();
                                getAlbum.setName(documentSnapshot.getString("name"));
                                getAlbum.setFavorite(documentSnapshot.getBoolean("isFavorite"));
                                getAlbum.setPublic(documentSnapshot.getBoolean("isPublic"));
                                getAlbum.setId(documentSnapshot.getString("id"));
                                getAlbum.setDate(documentSnapshot.getDate("date"));

                                allAlbums.add(getAlbum);
                            }
                            listener.onGetAlbumSuccess(allAlbums);
                        } else {
                            Log.d("Firebase", "Error getting albums", task.getException());
                        }
                    }
                });
    }

    // *******************************************//
    //               get photos                   //
    // *******************************************//
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

    // ************************************************* //
    //                      SEARCH                       //
    // ************************************************* //
    private ArrayList<Album> searchedAlbums;
    private ArrayList<Album> userAlbums;
    private int position;
    public void searchUserAlbums(final String searchString, final OnGetSearchAlbumsListener listener) {
        position = 0;
        searchedAlbums = new ArrayList<>();
        getAlbums(new OnGetAlbumListener() {
            @Override
            public void onGetAlbumSuccess(ArrayList<Album> albums) {
                userAlbums = albums;
                for (Album album : albums) {
                    if (album.getName().contains(searchString)) {
                        searchedAlbums.add(album);
                        listener.searchedAlbums(album, position);
                        position++;
                    }
                }
                searchUserImages(searchString, listener);
            }
        });
    }

    private void searchUserImages(final String searchString, final OnGetSearchAlbumsListener listener) {
        for (final Album album: userAlbums) {
            if (!isAlbumAlreadyAdded(album)) {
                getPhotos(album.getName(), new OnGetPhotosListener() {
                    @Override
                    public void onGetPhotosSuccess(ArrayList<Image> images) {
                        for (Image image: images) {
                            if (image.getLocation().contains(searchString)) {
                                searchedAlbums.add(album);
                                listener.searchedAlbums(album, position);
                                position++;
                                break;
                            }
                        }
                    }
                });
            }
        }
    }

    private ArrayList<Album> getPublicAlbums;
    public void searchPublicAlbums(final User user, final String searchString, final OnGetSearchAlbumsListener listener) {
        if (!user.getId().equals(firebaseAuth.getUid())) {
            Log.d("demo", "Searching user: " + user.getId());
            getPublicAlbums(user, new OnGetPublicAlbumsListener() {
                @Override
                public void getPublicAlbums(ArrayList<Album> albums) {
                    Log.d("demo", "User albums: " + user.getId() + " : " + albums.toString());
                    getPublicAlbums = new ArrayList<>();
                    getPublicAlbums = albums;
                    for (Album album : albums) {
                        if (album.getName().contains(searchString)) {
                            searchedAlbums.add(album);
                            listener.searchedAlbums(album, position);
                            position++;
                        }
                    }
                    searchPublicImages(searchString, listener);
                }
            });
        }
    }

    private void searchPublicImages(final String searchString, final OnGetSearchAlbumsListener listener) {
        for (final Album album : getPublicAlbums) {
            if (!isAlbumAlreadyAdded(album)) {
                getPhotos(album.getName(), new OnGetPhotosListener() {
                    @Override
                    public void onGetPhotosSuccess(ArrayList<Image> images) {
                        for (Image image : images) {
                            if (image.getLocation().contains(searchString)) {
                                searchedAlbums.add(album);
                                listener.searchedAlbums(album, position);
                                position++;
                                break;
                            }
                        }
                        listener.searchedAlbums(null, 0);
                    }
                });
            }
        }
    }

    private boolean isAlbumAlreadyAdded(Album album) {
        boolean alreadyAdded = false;
        for (Album alreadyAddedAlbum : searchedAlbums) {
            if (alreadyAddedAlbum.getName().equals(album.getName())) {
                alreadyAdded = true;
            }
        }
        return alreadyAdded;
    }
}
