package com.example.prm392_project;

import android.content.Intent;
import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.prm392_project.activity.CartActivity;
import com.example.prm392_project.fragments.ExplorerFragment;
import com.example.prm392_project.fragments.OrdersFragment;
import com.example.prm392_project.fragments.ProfileFragment;
import com.example.prm392_project.fragments.ShopFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_test);
        BottomNavigationView navbar = findViewById(R.id.nv_bottom2);
        navbar.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.ln_shop2){
                loadFragment(new ShopFragment());
                return true;
            }
            if(item.getItemId()==R.id.ln_profile2){
                loadFragment(new ProfileFragment());
                return true;
            }
            if(item.getItemId()==R.id.ln_explorer2){
                loadFragment(new ExplorerFragment());
                return true;
            }
            if(item.getItemId()==R.id.ln_favorites2) { // NOTE: It's actually cart
                Intent i = new Intent(this, CartActivity.class);
                startActivity(i);
                return true;
            }
            if(item.getItemId()==R.id.ln_orders2){
                loadFragment(new OrdersFragment());
                return true;
            }
            return false;
        });
        if (savedInstanceState == null) {
            loadFragment(new ExplorerFragment());
        }
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}