package com.example.prm392_project.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.models.ShippingAddress;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    private List<ShippingAddress> addresses;
    private AddressActionListener addressActionListener;

    public interface AddressActionListener {
        void onEditAddress(ShippingAddress address);
        void onDeleteAddress(ShippingAddress address);
        void onSetDefaultAddress(ShippingAddress address);
    }

    public AddressAdapter(List<ShippingAddress> addresses, AddressActionListener listener) {
        this.addresses = addresses;
        this.addressActionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(addresses.get(position));
    }

    @Override
    public int getItemCount() {
        return addresses != null ? addresses.size() : 0;
    }

    public void updateData(List<ShippingAddress> newAddresses) {
        this.addresses.clear();
        if (newAddresses != null) {
            for (ShippingAddress address : newAddresses) {
                if (address != null && 
                    address.getStreetAddress() != null && 
                    !address.getStreetAddress().isEmpty()) {
                    this.addresses.add(address);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStreetAddress, tvCity, tvState, tvDefaultIndicator;
        private final ImageButton btnEdit, btnDelete;
        private final Button btnSetDefault;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStreetAddress = itemView.findViewById(R.id.tvStreetAddress);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvState = itemView.findViewById(R.id.tvState);
            tvDefaultIndicator = itemView.findViewById(R.id.tvDefaultIndicator);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSetDefault = itemView.findViewById(R.id.btnSetDefault);
        }

        public void bind(ShippingAddress address) {
            tvStreetAddress.setText(address.getStreetAddress());
            tvCity.setText(address.getCity());
            tvState.setText(address.getState());

            // Show/hide default indicator and set default button
            if (address.isDefault()) {
                tvDefaultIndicator.setVisibility(View.VISIBLE);
                btnSetDefault.setVisibility(View.GONE);
            } else {
                tvDefaultIndicator.setVisibility(View.GONE);
                btnSetDefault.setVisibility(View.VISIBLE);
            }

            btnSetDefault.setOnClickListener(v -> addressActionListener.onSetDefaultAddress(address));
            btnEdit.setOnClickListener(v -> addressActionListener.onEditAddress(address));
            btnDelete.setOnClickListener(v -> addressActionListener.onDeleteAddress(address));
        }
    }
}