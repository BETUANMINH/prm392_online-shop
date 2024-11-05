package com.example.prm392_project.adminActivities;

import android.app.AlertDialog;
import android.os.Bundle;

import com.example.prm392_project.adapter.AdminOrderAdapter;
import com.example.prm392_project.models.Order;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.prm392_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private List<Order> orderList;
    private DatabaseReference ordersRef;
    private Spinner statusSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        orderList = new ArrayList<>();
        
        setupViews();
        setupSpinner();
        loadOrders();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(orderList, this::onProcessOrder, this::viewOrderDetails);
        recyclerView.setAdapter(adapter);
        
        statusSpinner = findViewById(R.id.spinnerStatus);
    }

    private void setupSpinner() {
        String[] statuses = {"All", "Pending", "Processing", "Shipping", "Delivered", "Cancelled"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, statuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerAdapter);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadOrders() {
        String selectedStatus = statusSpinner.getSelectedItem().toString();
        
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (selectedStatus.equals("All") || selectedStatus.equals(status)) {
                        String id = snapshot.getKey();
                        String total = snapshot.child("totalAmount").getValue().toString();
                        String date = snapshot.child("date").getValue(String.class);
                        
                        // Calculate total quantity and get first product image
                        long totalQuantity = 0;
                        String firstProductImg = null;
                        DataSnapshot itemsSnapshot = snapshot.child("items");
                        if (itemsSnapshot.exists()) {
                            for (DataSnapshot item : itemsSnapshot.getChildren()) {
                                totalQuantity += item.child("quantity").getValue(Long.class);
                                if (firstProductImg == null) {
                                    firstProductImg = item.child("productImg").getValue(String.class);
                                }
                            }
                        }
                        
                        Order order = new Order(id, total, totalQuantity, date, firstProductImg, status);
                        orderList.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrderActivity.this, "Failed to load orders", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void viewOrderDetails(Order order) {
        ordersRef.child(order.getId()).get().addOnSuccessListener(snapshot -> {
            StringBuilder details = new StringBuilder();
            details.append("Order Details\n\n");
            details.append("Date: ").append(order.getDate()).append("\n");
            details.append("Status: ").append(order.getStatus()).append("\n");
            details.append("Total Amount: $").append(order.getTotal()).append("\n\n");
            details.append("Items:\n");

            DataSnapshot itemsSnapshot = snapshot.child("items");
            for (DataSnapshot item : itemsSnapshot.getChildren()) {
                String name = item.child("name").getValue(String.class);
                long quantity = item.child("quantity").getValue(Long.class);
                double price = item.child("price").getValue(Double.class);
                details.append("- ").append(name)
                       .append(" (x").append(quantity).append(")")
                       .append(" $").append(price).append("\n");
            }

            // Show the detailed order dialog
            showOrderDetailsDialog(details.toString());
        });
    }

    private void onProcessOrder(Order order) {
        String[] options = {"Process Order", "Mark as Shipping", "Mark as Delivered", "Cancel Order"};
        new AlertDialog.Builder(this)
            .setTitle("Update Order Status")
            .setItems(options, (dialog, which) -> {
                String newStatus;
                switch (which) {
                    case 0:
                        newStatus = "Processing";
                        break;
                    case 1:
                        newStatus = "Shipping";
                        break;
                    case 2:
                        newStatus = "Delivered";
                        break;
                    case 3:
                        newStatus = "Cancelled";
                        break;
                    default:
                        return;
                }
                
                ordersRef.child(order.getId()).child("status").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update order status", 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .show();
    }

    private void showOrderDetailsDialog(String orderDetails) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_order_details, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set order details
        TextView tvOrderDetails = dialogView.findViewById(R.id.tvOrderDetails);
        tvOrderDetails.setText(orderDetails);

        // Set up the close button
        Button btnClose = dialogView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());
    }
}