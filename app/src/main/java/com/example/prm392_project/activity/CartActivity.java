package com.example.prm392_project.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.adapter.CartAdapter;
import com.example.prm392_project.helper.CartManagement;
import com.example.prm392_project.listener.CartItemChangeListener;
import com.example.prm392_project.models.CartItem;
import com.example.prm392_project.services.NavigationService;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private CartManagement cartManagement;
    private FirebaseDatabase database;
    private DatabaseReference cartRef;
    private RecyclerView cart;
    private FirebaseUser user;
    private TextView totalTxt;
    private TextView subTotalTxt;
    private ImageButton backBtn;
    private AppCompatButton checkoutBtn;
    DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManagement = new CartManagement();
        cart = findViewById(R.id.rv_cart);
        totalTxt = findViewById(R.id.tv_total);
        subTotalTxt = findViewById(R.id.tv_subtotal);
        backBtn = findViewById(R.id.btn_back);
        checkoutBtn = findViewById(R.id.btn_checkout);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationService.navigateBack(CartActivity.this);
            }
        });

        database = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadUserCart(user.getUid());
        checkoutBtn.setOnClickListener(view -> {
            ordersRef = database.getReference("Orders");
            cartRef = database.getReference("carts").child(user.getUid());
            String userId = user.getUid();

            cartRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    List<Map<String, Object>> items = new ArrayList<>();
                    long totalAmount = 0;

                    // Collect items from the cart and calculate total
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Map<String, Object> item = (Map<String, Object>) itemSnapshot.getValue();
                        items.add(item);
                        totalAmount += (long) item.get("price") * (long) item.get("quantity");
                        Log.d("bugorder", ""+totalAmount);
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = dateFormat.format(new Date());
                    // Prepare order data
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("userId", userId);
                    orderData.put("items", items);
                    orderData.put("totalAmount", totalAmount);
                    orderData.put("date", formattedDate);
                    orderData.put("status", "Pending");
                    // Add to "Orders" table
                    ordersRef.push().setValue(orderData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Remove items from the cart after successful order creation
                            cartRef.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CartActivity.this, "Checkout successful!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CartActivity.this, "Failed to clear cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(CartActivity.this, "Failed to place order: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CartActivity.this, "No items in cart.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(CartActivity.this, "Failed to retrieve cart items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

    }

    /**
     * Load user's cart from Firebase
     */
    private void loadUserCart(String userId) {
        cartRef = database.getReference("carts").child(userId);

        cart.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    TextView emptyCartTxt = findViewById(R.id.EmptyCartTxt);
                    emptyCartTxt.setVisibility(View.VISIBLE);
                } else {
                    List<CartItem> cartItems = new ArrayList<>();

                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        String name = itemSnapshot.child("name").getValue(String.class);
                        double price = itemSnapshot.child("price").getValue(Double.class);
                        int quantity = itemSnapshot.child("quantity").getValue(Integer.class);
                        String productImg = itemSnapshot.child("productImg").getValue(String.class);
                        String cartItemId = itemSnapshot.getKey(); // CartItemId in the format ItemId:Size
                        String[] ids = cartItemId.split(":");
                        String productId = ids[0];
                        int itemSize = Integer.parseInt(ids[1]);

                        Log.i("ITEM_IN_CART", "CartItemID: " + cartItemId);
                        cartItems.add(new CartItem(quantity, price, name, productImg, productId, itemSize));
                    }

                    // set data for adapter
                    cart.setAdapter(new CartAdapter(cartItems,
                            new CartItemChangeListener() {
                                @Override
                                public void onCartItemChanged() {
                                    calculateCart();
                                }
                            },
                            CartActivity.this
                    ));
                }

                calculateCart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Calculate total cost of all items in the cart
     * TODO: Move this method to CartManagement class
     */
    private void calculateCart() {

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double totalPrice = 0.0d;

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    double price = itemSnapshot.child("price").getValue(Double.class);
                    int quantity = itemSnapshot.child("quantity").getValue(Integer.class);

                    totalPrice += price * quantity;
                }

                totalTxt.setText(totalPrice + "");
                subTotalTxt.setText(totalPrice + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}