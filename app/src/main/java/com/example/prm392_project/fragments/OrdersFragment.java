package com.example.prm392_project.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.prm392_project.R;
import com.example.prm392_project.adapter.BrandAdapter;
import com.example.prm392_project.adapter.OrderItemAdapter;
import com.example.prm392_project.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrdersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrdersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OrdersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrdersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrdersFragment newInstance(String param1, String param2) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    private FirebaseUser user;
    private FirebaseDatabase database;
    DatabaseReference ordersRef;
    private RecyclerView rvOrders;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ordersRef = database.getReference("Orders");
        TextView tvNoOrders = view.findViewById(R.id.tv_no_orders);

        ordersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    tvNoOrders.setVisibility(View.VISIBLE);
                } else {
                    long totalQuantity = 0;
                    String firstProductImageUrl = null;
                    List<Order> orderList = new ArrayList<Order>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DataSnapshot itemsSnapshot = snapshot.child("items");
                        if (itemsSnapshot.exists() && itemsSnapshot.getChildrenCount() > 0) {
                            // Get the first item in the list
                            DataSnapshot firstItemSnapshot = itemsSnapshot.getChildren().iterator().next();

                            // Retrieve the product image URL of the first item
                            firstProductImageUrl = firstItemSnapshot.child("productImg").getValue(String.class);

                        }
                        for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                            // Get the quantity of each item
                            Long quantity = itemSnapshot.child("quantity").getValue(Long.class);

                            if (quantity != null) {
                                totalQuantity += quantity; // Add the quantity to the total
                            }
                        }
                        String date = snapshot.child("date").getValue().toString();
                        String total = snapshot.child("totalAmount").getValue().toString();
                        String id = snapshot.getKey();
                        String status = snapshot.child("status").getValue().toString();

                        orderList.add(new Order(id, total, totalQuantity, date, firstProductImageUrl, status));
                    }
                    OrderItemAdapter orderItemAdapter = new OrderItemAdapter(requireContext(), orderList);
                    rvOrders = view.findViewById(R.id.rv_orders);
                    rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
                    rvOrders.setAdapter(orderItemAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}