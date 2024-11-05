package com.example.prm392_project.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.adapter.ListProductApdapter;
import com.example.prm392_project.adapter.ShopFragmentAdapter;
import com.example.prm392_project.models.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShopFragment extends Fragment {
    private Button btnAddProduct;
    private RecyclerView recyclerView;
    private ListProductApdapter productAdapter;
    private ArrayList<Item> productList;
    private  ArrayList<Item> productListFilter;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference itemRef = database.getReference("Items");

    public ShopFragment() {
        // Required empty public constructor
    }

    public static ShopFragment newInstance(Long id, String name, String description, double price, String imageUrl) {
        ShopFragment fragment = new ShopFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("name", name);
        args.putString("description", description);
        args.putDouble("price", price);
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }
    // You can retrieve data like this in the fragment lifecycle methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        SearchView searchView = view.findViewById(R.id.searchProductView);
        Toolbar toolbar = view.findViewById(R.id.productToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        // Khởi tạo RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách sản phẩm và adapter
        productList = new ArrayList<>();
        productListFilter = new ArrayList<>();
        productAdapter = new ListProductApdapter(getContext(), productListFilter);
        recyclerView.setAdapter(productAdapter);

        loadProducts();
        // Thiết lập listener cho SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_product_menu, menu); // Inflate menu ở đây
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*switch (item.getItemId()) {
            case R.id.sortPrice:
                // Xử lý sự kiện thêm sản phẩm
                return true;
            case R.id.sortRate:
                // Xử lý sự kiện cài đặt
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
        return false;
    }
    private void loadProducts() {
        itemRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item product = snapshot.getValue(Item.class);
                    if (product != null) {
                        productList.add(product); // Thêm sản phẩm vào danh sách
                    }
                }
                productListFilter.clear();
                productListFilter.addAll(productList);
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi
            }
        });
    }
    // Phương thức để lọc danh sách sản phẩm
    @SuppressLint("NotifyDataSetChanged")
    private void filter(String text) {
        productListFilter.clear();
        if (text.isEmpty()) {
            productListFilter.addAll(productList);
        } else {
            text = text.trim().toLowerCase();
            Log.d("ShopFragment", text);
            for (Item product : productList) {
                if (product.getTitle().toLowerCase().contains(text)) {
                    productListFilter.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }
}
