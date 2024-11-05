package com.example.prm392_project.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.app.ProgressDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.prm392_project.R;
import com.example.prm392_project.auth.LoginActivity;
import com.example.prm392_project.models.ShippingAddress;
import com.example.prm392_project.models.User;
import com.example.prm392_project.test.UserDataManager;

import android.Manifest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.prm392_project.adapter.AddressAdapter;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 1;

    private ImageView profileImage;
    private EditText firstNameEdit, lastNameEdit, phoneEdit;
    private RadioGroup genderGroup;
    private Button saveProfileButton, btnManageAddresses;
    private TextView hiddenRole;

    private Uri imageUri;
    private UserDataManager userDataManager;

    private RecyclerView rvAddresses;
    private AddressAdapter addressAdapter;
    private Button btnAddAddress;
    private List<ShippingAddress> addresses = new ArrayList<>();

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userDataManager = new UserDataManager(getContext());
        initializeViews(view);
        loadUserData();
        requestStoragePermission();
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        firstNameEdit = view.findViewById(R.id.first_name);
        lastNameEdit = view.findViewById(R.id.last_name);
        genderGroup = view.findViewById(R.id.gender);
        phoneEdit = view.findViewById(R.id.phone);
        saveProfileButton = view.findViewById(R.id.save_profile);
        hiddenRole = view.findViewById(R.id.hidden_role);
        btnManageAddresses = view.findViewById(R.id.btn_manage_addresses);

        rvAddresses = view.findViewById(R.id.rvAddresses);
        btnAddAddress = view.findViewById(R.id.btnAddAddress);
        btnAddAddress.setOnClickListener(v -> showAddressDialog(null));

        rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));

        addressAdapter = new AddressAdapter(addresses, new AddressAdapter.AddressActionListener() {
            @Override
            public void onEditAddress(ShippingAddress address) {
                showAddressDialog(address);
            }

            @Override
            public void onDeleteAddress(ShippingAddress address) {
                deleteAddress(address);
            }

            @Override
            public void onSetDefaultAddress(ShippingAddress address) {
                setDefaultAddress(address);
            }
        });

        rvAddresses.setAdapter(addressAdapter);

        saveProfileButton.setOnClickListener(this);
        profileImage.setOnClickListener(this);
        btnManageAddresses.setOnClickListener(this);

        // Add logout button click listener
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Set initial visibility states
        rvAddresses.setVisibility(View.GONE);
        btnAddAddress.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_profile) {
            saveUserData();
        } else if (v.getId() == R.id.profile_image) {
            openFileChooser();
        } else if (v.getId() == R.id.btn_manage_addresses) {
            if (!userDataManager.isUserLoggedIn()) {
                Toast.makeText(getContext(), "Please log in to manage addresses", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rvAddresses.getVisibility() == View.VISIBLE) {
                rvAddresses.setVisibility(View.GONE);
                btnAddAddress.setVisibility(View.GONE);
                btnManageAddresses.setText("Manage Addresses");
            } else {
                rvAddresses.setVisibility(View.VISIBLE);
                btnAddAddress.setVisibility(View.VISIBLE);
                btnManageAddresses.setText("Hide Addresses");
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(profileImage);
        }
    }

    private void loadUserData() {
        String userId = userDataManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
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
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .into(profileImage);
                        }
                    }

                    // Load addresses with proper filtering
                    if (snapshot.child("addresses").exists()) {
                        List<ShippingAddress> addressList = new ArrayList<>();
                        for (DataSnapshot addressSnapshot : snapshot.child("addresses").getChildren()) {
                            ShippingAddress address = addressSnapshot.getValue(ShippingAddress.class);
                            // Only add valid addresses
                            if (address != null && 
                                address.getStreetAddress() != null && 
                                !address.getStreetAddress().isEmpty() &&
                                address.getCity() != null && 
                                !address.getCity().isEmpty() &&
                                address.getState() != null && 
                                !address.getState().isEmpty()) {
                                addressList.add(address);
                            } else {
                                // Remove invalid address from database
                                addressSnapshot.getRef().removeValue();
                            }
                        }
                        addresses = addressList; // Update the class-level list
                        addressAdapter.updateData(addressList);
                    } else {
                        addresses = new ArrayList<>();
                        addressAdapter.updateData(new ArrayList<>());
                    }

                    // Set role
                    if (snapshot.child("role").exists()) {
                        hiddenRole.setText(snapshot.child("role").getValue(String.class));
                    }

                } catch (Exception e) {
                    Log.e("ProfileFragment", "Error loading user data", e);
                    if (isAdded()) {
                        Toast.makeText(getContext(), 
                            "Error loading user data: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), 
                        "Error loading user data: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserData() {
        if (!userDataManager.isUserLoggedIn()) {
            Toast.makeText(getContext(), "You must be logged in to update your profile", Toast.LENGTH_SHORT).show();
            return;
        }

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
            Toast.makeText(getContext(), "Required fields cannot be empty", Toast.LENGTH_SHORT).show();
            return null;
        }

        String gender = getSelectedGender();
        if (gender == null) {
            Toast.makeText(getContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(getContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
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
        ProgressDialog progressDialog = new ProgressDialog(getContext());
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
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Upload failed: " + errorMessage);
            }

            @Override
            public void onProgress(int progress) {
                progressDialog.setMessage("Uploaded " + progress + "%");
            }
        });
    }

    private void updateUserProfile(User user) {
        // Remove the automatic creation of empty default address
        if (user.getAddresses() == null) {
            user.setAddresses(new ArrayList<>());
        }

        userDataManager.updateUserProfile(user, new UserDataManager.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                loadUserData();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(getContext(), "Failed to update profile: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        String phoneRegex = "^\\+?\\d{10,14}$";
        return phone.matches(phoneRegex);
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void addAddress(ShippingAddress address) {
        // Validate address before adding
        if (address == null || 
            address.getStreetAddress() == null || 
            address.getStreetAddress().isEmpty() ||
            address.getCity() == null || 
            address.getCity().isEmpty() ||
            address.getState() == null || 
            address.getState().isEmpty()) {
            Toast.makeText(getContext(), "Invalid address data", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userDataManager.getCurrentUserId())
            .child("addresses")
            .child(address.getId());

        userRef.setValue(address)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Address added successfully", Toast.LENGTH_SHORT).show();
                loadUserData();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to add address: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void updateAddress(ShippingAddress address) {
        DatabaseReference addressRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userDataManager.getCurrentUserId())
            .child("addresses")
            .child(address.getId());

        addressRef.setValue(address)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Address updated successfully", Toast.LENGTH_SHORT).show();
                loadUserData();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to update address: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void deleteAddress(ShippingAddress address) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Address")
            .setMessage("Are you sure you want to delete this address?")
            .setPositiveButton("Yes", (dialog, which) -> {
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userDataManager.getCurrentUserId());

                // If deleting default address, need to update defaultAddressId
                if (address.isDefault()) {
                    userRef.child("addresses").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Map<String, Object> updates = new HashMap<>();
                            
                            // Remove the address
                            updates.put("addresses/" + address.getId(), null);
                            
                            // Find another address to make default if this was the default
                            boolean foundNewDefault = false;
                            for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                                if (!addressSnapshot.getKey().equals(address.getId())) {
                                    String newDefaultId = addressSnapshot.getKey();
                                    updates.put("addresses/" + newDefaultId + "/default", true);
                                    updates.put("defaultAddressId", newDefaultId);
                                    foundNewDefault = true;
                                    break;
                                }
                            }
                            
                            // If no other address found, clear defaultAddressId
                            if (!foundNewDefault) {
                                updates.put("defaultAddressId", null);
                            }

                            userRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Address deleted successfully", 
                                        Toast.LENGTH_SHORT).show();
                                    loadUserData();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to delete address: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Error deleting address: " + error.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Simple delete for non-default address
                    userRef.child("addresses").child(address.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Address deleted successfully", 
                                Toast.LENGTH_SHORT).show();
                            loadUserData();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to delete address: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void setDefaultAddress(ShippingAddress newDefaultAddress) {
        // Validate the address data first
        if (newDefaultAddress == null || 
            newDefaultAddress.getStreetAddress() == null || 
            newDefaultAddress.getCity() == null || 
            newDefaultAddress.getState() == null) {
            Toast.makeText(getContext(), "Invalid address data", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = userDataManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.child("addresses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();

                // First validate that the address exists
                if (!snapshot.hasChild(newDefaultAddress.getId())) {
                    Toast.makeText(getContext(), "Address not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update all addresses to non-default
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    ShippingAddress address = addressSnapshot.getValue(ShippingAddress.class);
                    if (address != null) {
                        String addressId = addressSnapshot.getKey();
                        updates.put("addresses/" + addressId + "/default", false);
                    }
                }

                // Set the new default address
                String newDefaultId = newDefaultAddress.getId();
                updates.put("addresses/" + newDefaultId + "/default", true);
                updates.put("defaultAddressId", newDefaultId);

                // Ensure we're not overwriting other address data
                updates.put("addresses/" + newDefaultId + "/streetAddress", newDefaultAddress.getStreetAddress());
                updates.put("addresses/" + newDefaultId + "/city", newDefaultAddress.getCity());
                updates.put("addresses/" + newDefaultId + "/state", newDefaultAddress.getState());
                updates.put("addresses/" + newDefaultId + "/userId", userId);
                updates.put("addresses/" + newDefaultId + "/id", newDefaultId);

                // Perform batch update
                userRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Default address updated successfully", Toast.LENGTH_SHORT).show();
                            loadUserData();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to update default address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error updating default address: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddressDialog(ShippingAddress address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_address, null);
        builder.setView(dialogView);

        EditText etStreet = dialogView.findViewById(R.id.etStreet);
        EditText etCity = dialogView.findViewById(R.id.etCity);
        EditText etState = dialogView.findViewById(R.id.etState);

        builder.setTitle(address == null ? "Add New Address" : "Edit Address");

        // Populate fields if editing
        if (address != null) {
            etStreet.setText(address.getStreetAddress());
            etCity.setText(address.getCity());
            etState.setText(address.getState());
        }

        builder.setPositiveButton("Save", null); // Set to null initially to prevent dialog from dismissing on validation failure
        
        AlertDialog dialog = builder.create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                // Validate inputs
                String street = etStreet.getText().toString().trim();
                String city = etCity.getText().toString().trim();
                String state = etState.getText().toString().trim();

                if (street.isEmpty() || city.isEmpty() || state.isEmpty()) {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = userDataManager.getCurrentUserId();
                ShippingAddress newAddress;
                
                if (address != null) {
                    // Editing existing address
                    newAddress = new ShippingAddress(
                        address.getId(), // Keep existing ID
                        userId,
                        street,
                        city,
                        state,
                        address.isDefault() // Keep existing default status
                    );
                    updateAddress(newAddress);
                } else {
                    // Adding new address
                    String addressId = FirebaseDatabase.getInstance().getReference().push().getKey();
                    newAddress = new ShippingAddress(
                        addressId,
                        userId,
                        street,
                        city,
                        state,
                        addresses.isEmpty() // Make default if it's the first address
                    );
                    addAddress(newAddress);
                }
                dialog.dismiss();
            });
        });
        
        dialog.show();
    }
}
