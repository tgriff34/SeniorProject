package com.example.tristangriffin.projectx;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class UploadFilesTask extends AsyncTask<Uri, Void, Void>{

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageReference = storage.getReference();

    @Override
    protected Void doInBackground(Uri... uris) {
        final StorageReference fileRef = storageReference.child("images/public/" + user.getUid() + "/" + uris[0].getLastPathSegment());
        Log.d("Firebase", fileRef.toString());
        UploadTask uploadTask = fileRef.putFile(uris[0]);

        //Upload file to storage
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Firebase", "uploadSuccess: true");
                addToDatabase(fileRef);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firebase", "uploadSuccess: false", e);
            }
        });
        return null;
    }

    private void addToDatabase(StorageReference ref) {
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Map<String, Object> dbImageReference = new HashMap<>();
                dbImageReference.put("ref", uri.toString());
                db.collection("users")
                        .document(user.getUid())
                        .collection("images")
                        .add(dbImageReference)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Firebase", "dbRef:success");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "dbRef:failure");
                    }
                });
            }
        });
    }
}
