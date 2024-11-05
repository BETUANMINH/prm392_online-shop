package com.example.prm392_project.adapter;

import android.content.Context;
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
import com.example.prm392_project.models.Item;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 0;
    private static final int VIEW_TYPE_SIZE_QUANTITY = 1;

    private List<String> productUrls;
    private List<Item.SizeQuantity> sizeQuantityList;
    private Context context;

    public ProductAdapter(Context context, List<String> productUrls, List<Item.SizeQuantity> sizeQuantityList) {
        this.context = context;
        this.productUrls = productUrls;
        this.sizeQuantityList = sizeQuantityList;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine whether to show an image or size-quantity based on the position
        if (position < productUrls.size()) {
            return VIEW_TYPE_IMAGE; // First part is for images
        } else {
            return VIEW_TYPE_SIZE_QUANTITY; // After images, we show size and quantity
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item_container, parent, false);
            return new ProductViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.size_item, parent, false);
            return new SizeQuantityViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_IMAGE) {
            // Bind image URL to ImageView
            String url = productUrls.get(position);
            Log.d("ProductAdapter", "Loading image URL: " + url);
            ProductViewHolder productViewHolder = (ProductViewHolder) holder;
            Glide.with(context)
                    .load(url)
                    .into(productViewHolder.imageView);
        } else {
            // Bind size and quantity to respective TextViews
            int sizeQuantityPosition = position - productUrls.size(); // Adjust position for sizeQuantity list
            Item.SizeQuantity sizeQuantity = sizeQuantityList.get(sizeQuantityPosition);
            SizeQuantityViewHolder sizeQuantityViewHolder = (SizeQuantityViewHolder) holder;
            sizeQuantityViewHolder.sizeTextView.setText("Size: " + sizeQuantity.getSizeNumber());
            sizeQuantityViewHolder.quantityTextView.setText("Quantity: " + sizeQuantity.getQuantity());
        }
    }

    @Override
    public int getItemCount() {
        // Total items are sum of image URLs and size-quantity pairs
        return productUrls.size() + sizeQuantityList.size();
    }

    // ViewHolder for image slider
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }
    }

    // ViewHolder for size-quantity list
    public class SizeQuantityViewHolder extends RecyclerView.ViewHolder {
        TextView sizeTextView, quantityTextView;

        public SizeQuantityViewHolder(@NonNull View itemView) {
            super(itemView);
            sizeTextView = itemView.findViewById(R.id.tv_size);
            quantityTextView = itemView.findViewById(R.id.tv_quantity);
        }
    }
}
