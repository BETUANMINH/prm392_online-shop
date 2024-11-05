package com.example.prm392_project.adminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prm392_project.R;
//import com.example.prm392_project.services.AuthorizationService;
import com.example.prm392_project.adminActivities.CategoryAdminActivity;
import com.example.prm392_project.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboard extends AppCompatActivity {
    private DatabaseReference ordersRef;
    private TextView tvOrderCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase reference
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        // Initialize views
        tvOrderCount = findViewById(R.id.tvOrderCount);

        // Setup click listeners for cards (existing code)
        setupCardClickListeners();
        
        // Load order counts
        loadOrderCounts();
    }

    private void loadOrderCounts() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int pendingCount = 0;
                int processingCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (status != null) {
                        if (status.equals("Pending")) {
                            pendingCount++;
                        } else if (status.equals("Processing")) {
                            processingCount++;
                        }
                    }
                }

                // Update UI
                tvOrderCount.setText(pendingCount + " pending");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboard.this, "Failed to load order counts", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCardClickListeners() {
        MaterialCardView cardProfile = findViewById(R.id.cardProfile);
        MaterialCardView cardCategories = findViewById(R.id.cardCategories);
        MaterialCardView cardProducts = findViewById(R.id.cardProducts);
        MaterialCardView cardReports = findViewById(R.id.cardReports);
        MaterialCardView cardOrders = findViewById(R.id.cardOrders);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, ProfileAdminActivity.class);
            startActivity(intent);
        });

        cardCategories.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, CategoryAdminActivity.class);
            startActivity(intent);
        });

        cardProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, ProductAdminActivity.class);
            startActivity(intent);
        });

        cardReports.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, StatisticActivity.class);
            startActivity(intent);
        });

        cardOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminOrderActivity.class);
            intent.putExtra("filterStatus", "Pending");
            startActivity(intent);
        });


        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboard.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

