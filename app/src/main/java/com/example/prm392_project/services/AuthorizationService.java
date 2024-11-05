package com.example.prm392_project.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthorizationService {
    public interface RoleCallback {
        void onRoleChecked(boolean isAuthorized);
    }

    public static void checkUserRole(String requiredRole, RoleCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onRoleChecked(false);
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = dataSnapshot.child("role").getValue(String.class);
                    callback.onRoleChecked(requiredRole.equals(userRole));
                } else {
                    callback.onRoleChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onRoleChecked(false);
            }
        });
    }
}
