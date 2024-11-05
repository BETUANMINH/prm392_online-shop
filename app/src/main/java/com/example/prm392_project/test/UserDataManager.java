package com.example.prm392_project.test;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.prm392_project.models.ShippingAddress;
import com.example.prm392_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserDataManager {
    private static final String USERS_PATH = "users";
    private static final String PROFILE_IMAGES_PATH = "profile_images";

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    private final StorageReference mStorageRef;
    private final Context context;

    public UserDataManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference(PROFILE_IMAGES_PATH);
    }

    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null && !currentUser.isAnonymous();
    }

    public String getCurrentUserId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public void loadUserData(final UserDataCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        mDatabase.child(USERS_PATH).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    callback.onUserDataLoaded(user);
                } else {
                    callback.onError("Need to update profile to continue");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void uploadImage(Uri imageUri, User user, final UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure("No image selected");
            return;
        }

        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        StorageReference fileReference = mStorageRef.child(userId + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    callback.onSuccess(imageUrl);
                }))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()))
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    callback.onProgress((int) progress);
                });
    }

    public void updateUserProfile(User user, final UpdateCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        mDatabase.child(USERS_PATH).child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addAddress(ShippingAddress address, final UpdateCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("addresses")
            .push();

        address.setId(addressRef.getKey());
        addressRef.setValue(address)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateAddress(ShippingAddress address, final UpdateCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("addresses")
            .child(address.getId());

        addressRef.setValue(address)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteAddress(String addressId, final UpdateCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        DatabaseReference addressRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("addresses")
            .child(addressId);

        addressRef.removeValue()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void setDefaultAddress(String addressId, final UpdateCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId);

        userRef.child("defaultAddressId").setValue(addressId)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface UserDataCallback {
        void onUserDataLoaded(User user);
        void onError(String errorMessage);
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
        void onProgress(int progress);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
