package com.example.prm392_project.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.models.Order;

import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private OnOrderActionListener processListener;
    private OnOrderActionListener viewDetailsListener;

    public interface OnOrderActionListener {
        void onOrderAction(Order order);
    }

    public AdminOrderAdapter(List<Order> orderList, OnOrderActionListener processListener,
                             OnOrderActionListener viewDetailsListener) {
        this.orderList = orderList;
        this.processListener = processListener;
        this.viewDetailsListener = viewDetailsListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdText.setText("Order ID: " + order.getId());
        holder.dateText.setText("Date: " + order.getDate());
        holder.statusText.setText("Status: " + order.getStatus());
        holder.totalText.setText("Total: $" + order.getTotal());

        holder.viewDetailsButton.setOnClickListener(v -> viewDetailsListener.onOrderAction(order));
        holder.processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processListener.onOrderAction(order);
            }
        });

        // Disable process button for delivered orders
        if (order.getStatus().equals("Delivered")) {
            holder.processButton.setEnabled(false);
            holder.processButton.setAlpha(0.5f); // Optional: visually indicate the button is disabled
        } else {
            holder.processButton.setEnabled(true);
            holder.processButton.setAlpha(1.0f); // Reset alpha for enabled state
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText;
        TextView dateText;
        TextView statusText;
        TextView totalText;
        Button processButton;
        Button viewDetailsButton;

        OrderViewHolder(View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            dateText = itemView.findViewById(R.id.dateText);
            statusText = itemView.findViewById(R.id.statusText);
            totalText = itemView.findViewById(R.id.totalText);
            processButton = itemView.findViewById(R.id.processButton);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}
