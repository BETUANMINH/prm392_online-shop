package com.example.prm392_project.helper;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.activity.CartActivity;
import com.example.prm392_project.adapter.CartAdapter;
import com.example.prm392_project.listener.CartItemChangeListener;
import com.example.prm392_project.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManagement {
    public void addItemToCart(FirebaseUser user, CartItem item) {
        Log.d("ADD_TO_CART", "ProductId = " + item.getProductId());
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(user.getUid()).child(item.getCartItemId());

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // if the item exists, update quantity
                    int existingQuantity = snapshot.child("quantity").getValue(Integer.class);
                    cartRef.child("quantity").setValue(existingQuantity + 1);
                } else {
                    // if the item does not exist, add it
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("name", item.getName());
                    itemData.put("price", item.getPrice());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("productImg", item.getProductImg());

                    cartRef.setValue(itemData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "addItemToCart:onCancelled", error.toException());
            }
        });
    }
}
