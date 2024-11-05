package com.example.prm392_project.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_project.MainActivity;
import com.example.prm392_project.R;
import com.example.prm392_project.adminActivities.AdminDashboard;
import com.example.prm392_project.models.User;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.Arrays;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference itemRef = database.getReference("users");
    FirebaseAuth auth;
    TextView tvGoToRegister;
    TextView tvForgotPassword;
    GoogleSignInClient gsc;
    ImageView ivFacebookSignIn;
    ImageView ivGoogleSignIn;
    ImageView ivTwitterSignIn;
    CallbackManager callbackManager;
    EditText etEmail, etPassword;
    Button btnLogin;
    SharedPreferences sharedPreferences;

    // Google Sign-In Launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            // Authenticate with Firebase using Google account
                            firebaseAuthWithGoogle(account);
                        }
                    } catch (ApiException e) {
                        Log.e("GoogleSignInError", "Sign-in failed with code: " + e.getStatusCode());
                        Toast.makeText(LoginActivity.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    // Firebase authentication with Google Sign-In
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("GoogleSignIn", "firebaseAuthWithGoogle: " + account.getId());

        // Get the Google ID token
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        // Sign in to Firebase with the Google credentials
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Instead of directly navigating, first create/check user role
                String userId = auth.getCurrentUser().getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                
                userRef.child("role").get().addOnCompleteListener(roleTask -> {
                    if (roleTask.isSuccessful() && roleTask.getResult().exists()) {
                        // User exists with role, navigate based on role
                        navigateToMainActivity();
                    } else {
                        // New Google user, set default role as "User"
                        User newUser = new User(userId, account.getDisplayName(), "", "", "", "User");
                        userRef.setValue(newUser).addOnCompleteListener(createTask -> {
                            if (createTask.isSuccessful()) {
                                navigateToMainActivity();
                            } else {
                                Toast.makeText(LoginActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                Log.w("FirebaseAuthError", "signInWithCredential:failure", task.getException());
                Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        ivTwitterSignIn = findViewById(R.id.twitterBtn);
        // Initialise Facebook CallbackManager
        callbackManager = CallbackManager.Factory.create();

        // Set up Facebook login callback
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        String userId = auth.getCurrentUser().getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                        
                        userRef.child("role").get().addOnCompleteListener(roleTask -> {
                            if (roleTask.isSuccessful() && roleTask.getResult().exists()) {
                                navigateToMainActivity();
                            } else {
                                // Create new user with default role
                                User newUser = new User(userId, "", "", "", "", "User");
                                userRef.setValue(newUser).addOnCompleteListener(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        navigateToMainActivity();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Log.i("FacebookLogin", "onCancel: Facebook login cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e("FacebookLogin", "onError: Facebook login failed", exception);
                    }
                });

        // Set up UI elements
        tvGoToRegister = findViewById(R.id.goToRegister);
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvForgotPassword = findViewById(R.id.forgotpass);
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPassword.class)));

        ivGoogleSignIn = findViewById(R.id.googleBtn);
        ivFacebookSignIn = findViewById(R.id.fbBtn);

        // Google Sign-In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        // Google Sign-In button click
        ivGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // Facebook Sign-In button click
        ivFacebookSignIn.setOnClickListener(v -> signInWithFacebook());
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);

        btnLogin = findViewById(R.id.loginbtn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                if(username.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String userId = auth.getCurrentUser().getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                        
                        userRef.child("role").get().addOnCompleteListener(roleTask -> {
                            if (roleTask.isSuccessful()) {
                                navigateToMainActivity();
                            } else {
                                Toast.makeText(LoginActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        ivTwitterSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Intent intent = new Intent(LoginActivity.this, TwitterAuth.class);
                 intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                 startActivity(intent);


            }
        });


    }

    // Method to handle Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = gsc.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);

    }

    // Method to handle Facebook Sign-In
    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle Facebook login results
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // Method to navigate to the MainActivity after successful login
    private void navigateToMainActivity() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e("LoginActivity", "No user logged in");
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Log.d("LoginActivity", "Checking role for user ID: " + userId);
        Log.d("LoginActivity", "Database path: " + itemRef.child(userId).getPath().toString());
        
        itemRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                Log.d("LoginActivity", "Snapshot exists: " + snapshot.exists());
                Log.d("LoginActivity", "Snapshot value: " + snapshot.getValue());
                
                if (!snapshot.exists()) {
                    Log.e("LoginActivity", "User data not found for ID: " + userId);
                    // Create new user with default role
                    User newUser = new User(userId, "", "", "", "", "User");
                    itemRef.child(userId).setValue(newUser).addOnCompleteListener(createTask -> {
                        if (createTask.isSuccessful()) {
                            Log.d("LoginActivity", "Created new user with default role");
                            navigateToMainActivity(); // Retry navigation after creating user
                        } else {
                            Log.e("LoginActivity", "Failed to create new user", createTask.getException());
                            Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                
                // Get role directly from the snapshot
                String role = snapshot.child("role").getValue(String.class);
                Log.d("LoginActivity", "Role value retrieved: " + role);
                
                Intent intent;
                if ("Admin".equals(role)) {
                    Log.d("LoginActivity", "Role matches Admin, navigating to AdminDashboard");
                    intent = new Intent(this, AdminDashboard.class);
                } else {
                    Log.d("LoginActivity", "Role does not match Admin (role='" + role + "'), navigating to MainActivity");
                    intent = new Intent(this, MainActivity.class);
                }
                
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.e("LoginActivity", "Failed to get user data", task.getException());
                Toast.makeText(this, "Failed to determine user role", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && !isFinishing()) {
            String userId = currentUser.getUid();
            Log.d("LoginActivity", "onStart - Checking role for user: " + userId);
            
            itemRef.child(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists() && !isFinishing()) {
                    DataSnapshot snapshot = task.getResult();
                    Map<String, Object> userData = (Map<String, Object>) snapshot.getValue();
                    Log.d("LoginActivity", "onStart - Full user data: " + userData);
                    
                    String role = userData != null ? (String) userData.get("role") : null;
                    Log.d("LoginActivity", "onStart - User role: " + role);
                    
                    Intent intent;
                    if ("Admin".equals(role)) {
                        Log.d("LoginActivity", "onStart - Role matches Admin, navigating to AdminDashboard");
                        intent = new Intent(this, AdminDashboard.class);
                    } else {
                        Log.d("LoginActivity", "onStart - Role does not match Admin (role='" + role + "'), navigating to MainActivity");
                        intent = new Intent(this, MainActivity.class);
                    }
                    
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
