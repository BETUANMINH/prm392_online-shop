package com.example.prm392_project.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_project.MainActivity;
import com.example.prm392_project.R;
import com.example.prm392_project.adapter.BannerAdapter;
import com.example.prm392_project.adapter.BrandAdapter;
import com.example.prm392_project.adapter.ItemListAdapter;
import com.example.prm392_project.auth.LoginActivity;
import com.example.prm392_project.models.Item;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivityTest extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference bannerRef = database.getReference("Banner");
    DatabaseReference brandRef = database.getReference("Category");
    DatabaseReference itemRef = database.getReference("Items");
    TextView tvusername;
    ImageView logout;
    private RecyclerView recyclerViewBrands;
    private BrandAdapter brandAdapter;
    private List<String> brandUrls;
    private List<String> brandNames;

    private RecyclerView recyclerViewItems;
    private ItemListAdapter itemListAdapter;
    private List<Item> itemList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvusername = findViewById(R.id.tv_username);
        logout = findViewById(R.id.logout);

        // Get the currently signed-in Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // If the user is logged in, display their name or email
            String username = user.getDisplayName();
            if (username == null || username.isEmpty()) {
                username = user.getEmail(); // Fallback to email if display name is not available
            }
            tvusername.setText(username);
        } else {
            // If the user is not logged in, redirect to the login page
            Intent intent = new Intent(MainActivityTest.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Logout button click listener
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        bannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> bannerUrls = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.child("url").getValue(String.class);
                    if (url != null) {
                        bannerUrls.add(url);
                    }
                }

                ViewPager2 viewPager2 = findViewById(R.id.viewpagerSlider);
                BannerAdapter adapter = new BannerAdapter(MainActivityTest.this, bannerUrls);
                viewPager2.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching data: " + error.getMessage());
            }
        });

        recyclerViewBrands = findViewById(R.id.rv_brand);
        recyclerViewBrands.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        // Initialize brand URL list
        brandUrls = new ArrayList<>();
        brandNames = new ArrayList<>();

        // Retrieve data from Firebase
        brandRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                brandUrls.clear(); // Clear list before adding new data
                brandNames.clear(); // Clear list before adding new data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String brandUrl = dataSnapshot.child("picUrl").getValue(String.class);
                    String brandName = dataSnapshot.child("title").getValue(String.class);
                    if (brandUrl != null&&brandName!=null) {
                        brandUrls.add(brandUrl);
                        brandNames.add(brandName);
                    }
                }
                // Initialize and set the adapter after data is loaded
                brandAdapter = new BrandAdapter(MainActivityTest.this, brandUrls, brandNames);
                recyclerViewBrands.setAdapter(brandAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "onCancelled: " + error.getMessage());
            }
        });

        recyclerViewItems = findViewById(R.id.rv_items);
        recyclerViewItems.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize brand URL list
        itemList = new ArrayList<>();

        // Retrieve data from Firebase
        itemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        itemList.add(item);  // Add each item to the list
                    }
                }
                itemListAdapter = new ItemListAdapter(MainActivityTest.this, itemList);
                recyclerViewItems.setAdapter(itemListAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInClient gsc = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
        gsc.signOut().addOnCompleteListener(this, task -> {
            // Go back to the login activity or any other post-sign-out action
            startActivity(new Intent(MainActivityTest.this, LoginActivity.class));
            finish();
        });
    }
}