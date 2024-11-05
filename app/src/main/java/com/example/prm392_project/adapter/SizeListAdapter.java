package com.example.prm392_project.adapter;

import android.content.Context;
import android.graphics.Color;
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

public class SizeListAdapter extends RecyclerView.Adapter<SizeListAdapter.SizeListViewHolder> {
    private List<String> sizeList;
    private Context context;
    private int selectedPosition = -1;
    public SizeListAdapter(Context context, List<String> sizeList) {
        this.context = context;
        this.sizeList = sizeList;
    }
    public class SizeListViewHolder extends RecyclerView.ViewHolder {
        TextView tvSize;
        public SizeListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSize = itemView.findViewById(R.id.tv_size);
        }
    }
    @NonNull
    @Override
    public SizeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_size, parent, false);
        return new SizeListAdapter.SizeListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeListAdapter.SizeListViewHolder holder, int position) {
        String size = sizeList.get(position);
        holder.tvSize.setText(size);
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#6200EE")); // Selected color
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#D3D3D3")); // Default color
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the selected position
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged(); // Refresh the RecyclerView
            }
        });
    }

    @Override
    public int getItemCount() {
        return sizeList.size();
    }
}
