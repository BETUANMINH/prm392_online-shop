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
import com.example.prm392_project.listener.CartItemChangeListener;
import com.example.prm392_project.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems; // items in the cart
    private CartItemChangeListener itemChanged;
    private Context context;

    public CartAdapter(List<CartItem> cartItems, CartItemChangeListener itemChanged, Context context) {
        this.cartItems = cartItems;
        this.context = context;
        this.itemChanged = itemChanged;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View cartItem = inflater.inflate(R.layout.viewholder_cart, parent, false);

        CartViewHolder cartViewHolder = new CartViewHolder(cartItem);
        return cartViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.title.setText(item.getName());
        holder.size.setText("Size: " + item.getSize());
        holder.feeEachItem.setText("$" + item.getPrice());
        holder.totalEachItem.setText("$" + (item.getQuantity() * item.getPrice()));
        holder.quantity.setText(item.getQuantity() + "");

        // load image from Firebase to ImageView using Glide
        Glide.with(context)
                .load(item.getProductImg())
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView size;
        ImageView imgProduct;
        TextView feeEachItem;
        TextView totalEachItem;
        TextView quantity;
        TextView plusItem;
        TextView minusItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.titleTxt);
            size = itemView.findViewById(R.id.sizeTxt);
            imgProduct = itemView.findViewById(R.id.imageProduct);
            feeEachItem = itemView.findViewById(R.id.feeEachItem);
            totalEachItem = itemView.findViewById(R.id.totalEachItem);
            quantity = itemView.findViewById(R.id.quantity);
            plusItem = itemView.findViewById(R.id.plusCartBtn);
            minusItem = itemView.findViewById(R.id.minusCartBtn);

            plusItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Validate data
                    int position = getAdapterPosition();
                    CartItem item = cartItems.get(position);
                    String cartItemId = item.getCartItemId();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(user.getUid()).child(cartItemId);

                    cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                int oldQuantity = snapshot.child("quantity").getValue(Integer.class);
                                Log.i("PLUS_ITEM_CART", "ProductId: " + cartItemId + ", old quantity: " + oldQuantity);

                                cartRef.child("quantity").setValue(oldQuantity + 1);
                                item.setQuantity(oldQuantity + 1);
                                notifyItemChanged(position);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    // raise the CartItemChange event
                    itemChanged.onCartItemChanged();
                }
            });

            minusItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Validate data
                    int position = getAdapterPosition();
                    CartItem item = cartItems.get(position);
                    String cartItemId = item.getCartItemId();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(user.getUid()).child(cartItemId);

                    cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                int oldQuantity = snapshot.child("quantity").getValue(Integer.class);
                                Log.i("MINUS_ITEM_CART", "ProductId: " + cartItemId + ", old quantity: " + oldQuantity);

                                if (oldQuantity - 1 == 0) { // if quantity reaches 0, remove item from cart
                                    cartItems.remove(item);
                                    cartRef.removeValue();
                                    notifyItemRemoved(position);
                                } else {
                                    cartRef.child("quantity").setValue(oldQuantity - 1);
                                    item.setQuantity(oldQuantity - 1);
                                    notifyItemChanged(position);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    // raise the CartItemChange event
                    itemChanged.onCartItemChanged();
                }
            });
        }
    }


}
