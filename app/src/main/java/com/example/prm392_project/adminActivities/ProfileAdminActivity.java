package com.example.prm392_project.adminActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.prm392_project.R;
import com.example.prm392_project.models.User;
import com.example.prm392_project.test.UserDataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileAdminActivity extends AppCompatActivity {
    private static final String TAG = "ProfileAdminActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText firstNameEdit, lastNameEdit, phoneEdit;
    private RadioGroup genderGroup;
    private TextView hiddenRole;
    private Button saveButton;
    private Uri imageUri;
    private UserDataManager userDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);
        
        userDataManager = new UserDataManager(this);
        initializeViews();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profile_image);
        firstNameEdit = findViewById(R.id.first_name);
        lastNameEdit = findViewById(R.id.last_name);
        phoneEdit = findViewById(R.id.phone);
        genderGroup = findViewById(R.id.gender_group);
        hiddenRole = findViewById(R.id.hidden_role);
        saveButton = findViewById(R.id.save_button);
    }

    private void setupListeners() {
        profileImage.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK 
            && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "No user data found");
                    return;
                }

                // Update UI with user data
                if (snapshot.child("firstName").exists()) 
                    firstNameEdit.setText(snapshot.child("firstName").getValue(String.class));
                if (snapshot.child("lastName").exists()) 
                    lastNameEdit.setText(snapshot.child("lastName").getValue(String.class));
                if (snapshot.child("phone").exists()) 
                    phoneEdit.setText(snapshot.child("phone").getValue(String.class));
                
                // Set gender
                if (snapshot.child("gender").exists()) {
                    String gender = snapshot.child("gender").getValue(String.class);
                    if ("Male".equals(gender)) {
                        genderGroup.check(R.id.male);
                    } else if ("Female".equals(gender)) {
                        genderGroup.check(R.id.female);
                    }
                }

                // Load profile image
                if (snapshot.child("profileImageUrl").exists()) {
                    String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(ProfileAdminActivity.this)
                            .load(imageUrl)
                            .into(profileImage);
                    }
                }

                // Set role
                if (snapshot.child("role").exists()) {
                    hiddenRole.setText(snapshot.child("role").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileAdminActivity.this, 
                    "Error loading user data: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        User user = createUserFromInput();
        if (user == null) return;

        if (imageUri != null) {
            uploadImageAndSaveUser(user);
        } else {
            updateUserProfile(user);
        }
    }

    private User createUserFromInput() {
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String phone = phoneEdit.getText().toString().trim();
        String role = hiddenRole.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Required fields cannot be empty", Toast.LENGTH_SHORT).show();
            return null;
        }

        String gender = getSelectedGender();
        if (gender == null) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return null;
        }

        return new User(userDataManager.getCurrentUserId(), firstName, lastName, gender, phone, role);
    }

    private String getSelectedGender() {
        int selectedId = genderGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.male) {
            return "Male";
        } else if (selectedId == R.id.female) {
            return "Female";
        }
        return null;
    }

    private void uploadImageAndSaveUser(User user) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        userDataManager.uploadImage(imageUri, user, new UserDataManager.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                progressDialog.dismiss();
                user.setProfileImageUrl(imageUrl);
                updateUserProfile(user);
            }

            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(ProfileAdminActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Upload failed: " + errorMessage);
            }

            @Override
            public void onProgress(int progress) {
                progressDialog.setMessage("Uploaded " + progress + "%");
            }
        });
    }

    private void updateUserProfile(User user) {
        userDataManager.updateUserProfile(user, new UserDataManager.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ProfileAdminActivity.this, 
                    "Profile updated successfully", Toast.LENGTH_SHORT).show();
                loadUserData();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ProfileAdminActivity.this, 
                    "Failed to update profile: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}