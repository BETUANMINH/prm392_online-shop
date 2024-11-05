package com.example.prm392_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.prm392_project.R;
import com.example.prm392_project.adapter.ProductAdapter;
import com.example.prm392_project.helper.CartManagement;
import com.example.prm392_project.helper.StringHelper;
import com.example.prm392_project.models.CartItem;
import com.example.prm392_project.models.Item;
import com.example.prm392_project.services.NavigationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProductDetail extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton openCartBtn;
    private Button buyNowBtn;
    private Spinner spinnerSizes;
    private String selectedSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get Item from intent
        Intent intent = getIntent();
        Item item = intent.getParcelableExtra("item");

        if (item == null) {
            Log.e("ProductDetail", "No item data found!");
            return;
        }

        // Retrieve the list of picture URLs and SizeQuantity list
        List<String> picURLs = item.getPicUrl();
        List<Item.SizeQuantity> sizeQuantityList = item.getSizeQuantity();
        Log.d("ProductDetail", "Number of image URLs: " + (picURLs != null ? picURLs.size() : 0));

        // Setup ViewPager for image slider
        ProductAdapter productAdapter = new ProductAdapter(ProductDetail.this, picURLs, sizeQuantityList);
        ViewPager2 viewPagerItem = findViewById(R.id.imgslider);
        viewPagerItem.setAdapter(productAdapter);

        // Setup UI elements for product information
        TextView price = findViewById(R.id.tv_price);
        TextView rating = findViewById(R.id.tv_rating);
        TextView title = findViewById(R.id.tv_title);
        TextView description = findViewById(R.id.tv_description);

        // Set values to the UI
        price.setText(item.getPrice() != null ? "$" + item.getPrice().toString() : "Price unavailable");
        rating.setText(String.valueOf(item.getRating()));
        title.setText(item.getTitle());
        description.setText(item.getDescription());

        // Setup Spinner for size selection
        spinnerSizes = findViewById(R.id.spinner_sizes);

        // Create a list of size options from SizeQuantity
        List<String> sizeOptions = new ArrayList<>();
        for (Item.SizeQuantity sizeQuantity : sizeQuantityList) {
            sizeOptions.add("Size: " + sizeQuantity.getSizeNumber()); // Add size values to the list
        }

        // Set up ArrayAdapter for the Spinner
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sizeOptions);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSizes.setAdapter(sizeAdapter);

        // Set a listener to handle the selection of a size
        spinnerSizes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSize = sizeOptions.get(position); // Update the selected size when user selects
                Toast.makeText(ProductDetail.this, "Selected size: " + selectedSize, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle when no size is selected, if necessary
            }
        });

        // Setup back button to navigate back
        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> NavigationService.navigateBack(ProductDetail.this));

        // Open cart from ProductDetails activity
        openCartBtn = findViewById(R.id.btn_OpenCart);
        openCartBtn.setOnClickListener(v -> {
            Intent i = new Intent(ProductDetail.this, CartActivity.class);
            startActivity(i);
        });

        // Add this product to cart
        buyNowBtn = findViewById(R.id.btn_BuyNow);
        buyNowBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                CartItem cartItem = new CartItem(item, StringHelper.getSelectedSize(selectedSize));
                // Add selected size to cartItem or other logic
                Toast.makeText(ProductDetail.this, "Buying product with size: " + selectedSize, Toast.LENGTH_SHORT).show();

                CartManagement cartManagement = new CartManagement();
                cartManagement.addItemToCart(user, cartItem);
            } else {
                Log.e("ProductDetail", "User not logged in!");
            }
        });
    }
}
