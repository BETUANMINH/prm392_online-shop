package com.example.prm392_project.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.prm392_project.R;
import com.example.prm392_project.adapter.BannerAdapter;
import com.example.prm392_project.adapter.BrandAdapter;
import com.example.prm392_project.adapter.ItemListAdapter;
import com.example.prm392_project.auth.LoginActivity;
import com.example.prm392_project.models.Item;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExplorerFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference bannerRef = database.getReference("Banner");
    DatabaseReference brandRef = database.getReference("Category");
    DatabaseReference itemRef = database.getReference("Items");
    TextView tvusername;
    private RecyclerView recyclerViewBrands;
    private BrandAdapter brandAdapter;
    private List<String> brandUrls;
    private List<String> brandNames;

    private RecyclerView recyclerViewItems;
    private ItemListAdapter itemListAdapter;
    private List<Item> itemList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explorer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvusername = view.findViewById(R.id.tv_username);

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
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }

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

                ViewPager2 viewPager2 = view.findViewById(R.id.viewpagerSlider);
                BannerAdapter adapter = new BannerAdapter(requireContext(), bannerUrls);
                viewPager2.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching data: " + error.getMessage());
            }
        });
        recyclerViewBrands = view.findViewById(R.id.rv_brand);
        recyclerViewBrands.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        brandUrls = new ArrayList<>();
        brandNames = new ArrayList<>();

        // Load Brand Data
        brandRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                brandUrls.clear();
                brandNames.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String brandUrl = dataSnapshot.child("picUrl").getValue(String.class);
                    String brandName = dataSnapshot.child("title").getValue(String.class);
                    if (brandUrl != null && brandName != null) {
                        brandUrls.add(brandUrl);
                        brandNames.add(brandName);
                    }
                }

                brandAdapter = new BrandAdapter(requireContext(), brandUrls, brandNames);
                recyclerViewBrands.setAdapter(brandAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "onCancelled: " + error.getMessage());
            }
        });
        recyclerViewItems = view.findViewById(R.id.rv_items);
        recyclerViewItems.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        itemList = new ArrayList<>();

        // Load Item Data
        itemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }

                itemListAdapter = new ItemListAdapter(requireContext(), itemList);
                recyclerViewItems.setAdapter(itemListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "onCancelled: " + error.getMessage());
            }
        });
    }
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInClient gsc = GoogleSignIn.getClient(requireContext(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
        gsc.signOut().addOnCompleteListener(requireActivity(), task -> {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}