package com.example.prm392_project.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_project.R;
import com.example.prm392_project.activity.ProductDetail;
import com.example.prm392_project.models.Item;

import java.util.List;

public class ListProductApdapter extends RecyclerView.Adapter<ListProductApdapter.ViewHolder>{
    private Context context;
    private List<Item> itemList;

    public ListProductApdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ListProductApdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_layout, parent, false);
        Log.d("loggingcreate", "oncreateviewholder");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListProductApdapter.ViewHolder holder, int position) {
        Item item = itemList.get(position); // Get the current item
        String url = item.getPicUrl().get(0);
        holder.productName.setText(String.valueOf(item.getTitle()));
        holder.productPrice.setText(String.valueOf(item.getPrice()));
        holder.productDescription.setText(String.valueOf(item.getDescription()));
        Log.d("loggingbind", position+"  "+url);
        Glide.with(context)
                .load(url)
                .into(holder.imgProduct);

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productDescription;
        TextView productPrice;
        ImageView imgProduct;
        SearchView searchView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            searchView = itemView.findViewById(R.id.searchView);
        }
    }
}
