package com.example.prm392_project.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_project.R;
import com.example.prm392_project.models.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private Context context;
    private List<Order> orderList;

    public OrderItemAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    public class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTotal;
        TextView tvDate;
        ImageView ivOrder;
        TextView tvQuantity;
        TextView tvId;
        TextView tvStatus;
        AppCompatButton btnCancelOrder;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_Id);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            ivOrder = itemView.findViewById(R.id.iv_orderimg);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnCancelOrder = itemView.findViewById(R.id.btn_order_cancel);

        }
    }

    @NonNull
    @Override
    public OrderItemAdapter.OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_myorderitem, parent, false);
        return new OrderItemAdapter.OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        Order item = orderList.get(position); // Get the current item
        holder.tvTotal.setText(item.getTotal());
        holder.tvDate.setText(item.getDate());
        holder.tvId.setText(item.getId());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvStatus.setText(String.valueOf(item.getStatus()));
        Glide.with(context)
                .load(item.getProductImg())
                .into(holder.ivOrder);
        if (item.getStatus().equals("Cancelled") || item.getStatus().equals("Shipped")) {
            holder.btnCancelOrder.setVisibility(View.INVISIBLE);
        }
        holder.btnCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
                ordersRef.child(item.getId()).child("status").setValue("Cancelled")
                        .addOnSuccessListener(aVoid -> {
                            // Update UI after successfully canceling the order
                            holder.btnCancelOrder.setVisibility(View.INVISIBLE);
                            holder.tvStatus.setText("Cancelled");
                            Toast.makeText(context, "Order canceled successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to cancel order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }
}