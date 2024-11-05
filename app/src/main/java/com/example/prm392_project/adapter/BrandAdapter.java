package com.example.prm392_project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_project.R;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {
    private List<String> brandUrls;
    private Context context;
    private List<String> brandNames;

    public BrandAdapter(Context context, List<String> brandUrls, List<String> brandNames) {
        this.context = context;
        this.brandUrls = brandUrls;
        this.brandNames = brandNames;
    }
    public class BrandViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBrand;
        TextView tvBrand;
        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBrand = itemView.findViewById(R.id.iv_brand);
            tvBrand = itemView.findViewById(R.id.tv_brand);
        }
    }
    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_brand, parent, false);
        return new BrandAdapter.BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandAdapter.BrandViewHolder holder, int position) {
        String url = brandUrls.get(position);
        String brandname = brandNames.get(position);

        Glide.with(context)
                .load(url)
                .into(holder.ivBrand);
        holder.tvBrand.setText(brandname);
    }

    @Override
    public int getItemCount() {
        return brandUrls.size();
    }
}
