package com.example.prm392_project.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_project.R;
import com.example.prm392_project.activity.ProductDetail;
import com.example.prm392_project.models.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListViewHolder> {
    private Context context;
    private List<Item> itemList;

    public ItemListAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }
    public class ItemListViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrice;
        TextView tvRating;
        ImageView ivItem;

        public ItemListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.txt_price);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ivItem = itemView.findViewById(R.id.iv_product);
        }
    }
    @NonNull
    @Override
    public ItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_recommended, parent, false);
        Log.d("loggingcreate", "oncreateviewholder");
        return new ItemListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListAdapter.ItemListViewHolder holder, int position) {
        Item item = itemList.get(position); // Get the current item
        String url = item.getPicUrl().get(0);
        holder.tvPrice.setText(String.valueOf(item.getPrice()));
        holder.tvRating.setText(String.valueOf(item.getRating()));
        Log.d("loggingbind", position+"  "+url);
        Glide.with(context)
                .load(url)
                .into(holder.ivItem);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetail.class);
            intent.putExtra("item", item); // Pass the entire item object
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}