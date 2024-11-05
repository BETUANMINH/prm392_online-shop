package com.example.prm392_project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.models.Item;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder> {

    private Context context;
    private List<Item> productList;
    private OnProductClickListener onProductClickListener;

    // Interface để xử lý các sự kiện click
    public interface OnProductClickListener {
        void onProductClick(Item product);
        void onUpdateClick(Item product);
        void onToggleActiveClick(Item product); // Sự kiện kích hoạt/vô hiệu hóa sản phẩm
    }

    public ProductAdminAdapter(Context context, List<Item> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.onProductClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Item product = productList.get(position);
        holder.tvProductTitle.setText(product.getTitle());

        // Kiểm tra xem picUrl có null hoặc rỗng không trước khi tải ảnh
        if (product.getPicUrl() != null && !product.getPicUrl().isEmpty()) {
            Picasso.get().load(product.getPicUrl().get(0)).into(holder.imgProduct);
        } else {
            // Đặt một ảnh mặc định hoặc xử lý khi không có ảnh
            holder.imgProduct.setImageResource(R.drawable.placeholder_image);
        }

        // Hiển thị chi tiết sản phẩm khi nhấn nút "View"
        holder.btnViewDetails.setOnClickListener(v -> onProductClickListener.onProductClick(product));

        // Sự kiện chỉnh sửa sản phẩm khi nhấn nút "Update"
        holder.btnUpdateProduct.setOnClickListener(v -> onProductClickListener.onUpdateClick(product));

        // Thiết lập nút "Active/Disable" tùy vào trạng thái hiện tại của sản phẩm
        if (product.isActive()) {
            holder.btnToggleActive.setText("Disable");
        } else {
            holder.btnToggleActive.setText("Activate");
        }

        // Sự kiện chuyển đổi trạng thái kích hoạt/vô hiệu hóa sản phẩm
        holder.btnToggleActive.setOnClickListener(v -> {
            if (product.isActive()) {
                product.setActive(false); // Vô hiệu hóa sản phẩm
                holder.btnToggleActive.setText("Activate");
            } else {
                product.setActive(true); // Kích hoạt lại sản phẩm
                holder.btnToggleActive.setText("Disable");
            }
            onProductClickListener.onToggleActiveClick(product); // Cập nhật trạng thái trong Firebase hoặc DB
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder class để giữ các view cho mỗi item
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductTitle;
        ImageView imgProduct;
        Button btnViewDetails, btnUpdateProduct, btnToggleActive;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnUpdateProduct = itemView.findViewById(R.id.btnUpdateProduct);
            btnToggleActive = itemView.findViewById(R.id.btnToggleActive);
        }
    }
}
