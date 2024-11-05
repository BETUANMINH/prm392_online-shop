package com.example.prm392_project.adminActivities;
//123123
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView; // Đúng import của androidx
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.adapter.ProductAdminAdapter;
import com.example.prm392_project.models.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.example.prm392_project.models.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdminActivity extends AppCompatActivity {
    private View dialogView;
    private ImageView imgProductPreviewForDialog;
    private RecyclerView productRecyclerView;
    private ProductAdminAdapter productAdapter;
    private ArrayList<Item> productList = new ArrayList<>();
    private DatabaseReference productRef;
    private StorageReference storageRef;
    private Uri imageUri; // To store the selected image URI

    private SearchView searchView;
    private Spinner spinnerCategory, spinnerSize, spinnerStatus;
    private List<Item> filteredProductList = new ArrayList<>();

    private String currentStatusFilter = "Status: All";
    private String currentSizeFilter = "Size: All";
    private String currentCategoryFilter = "Category: All";
    private String currentSearchQuery = "";

    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_admin);

        // Khởi tạo các thành phần giao diện
        productRecyclerView = findViewById(R.id.productRecyclerView);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firebase references
        productRef = FirebaseDatabase.getInstance().getReference("Items");
        storageRef = FirebaseStorage.getInstance().getReference("ProductImage");

        searchView = findViewById(R.id.searchView);
        spinnerCategory = findViewById(R.id.spinnerCategory); // Lấy spinner từ layout activity
        spinnerSize = findViewById(R.id.spinnerSize);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        // Thiết lập tìm kiếm và bộ lọc
        setupSearchAndFilters();

        // Set up RecyclerView adapter
        productAdapter = new ProductAdminAdapter(this, filteredProductList, new ProductAdminAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Item product) {
                showViewDialog(product); // Hiển thị dialog chỉ xem
            }

            @Override
            public void onUpdateClick(Item product) {
                showUpdateDialog(product); // Hiển thị dialog chỉnh sửa sản phẩm
            }

            @Override
            public void onToggleActiveClick(Item product) {
                updateProductInFirebase(product); // Chuyển đổi trạng thái kích hoạt sản phẩm
            }
        });

        productRecyclerView.setAdapter(productAdapter);

        // Gọi hàm loadCategories để tải danh sách danh mục
        loadCategories();

        // Tải danh sách sản phẩm từ Firebase
        loadProducts();

        // Nút thêm sản phẩm
        findViewById(R.id.btnAddProduct).setOnClickListener(v -> showAddProductDialog());
    }




    private void loadCategories() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                populateCategoryFilter(); // Sau khi tải xong, cập nhật vào Spinner cho bộ lọc
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductAdminActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private String getCategoryIdByTitle(String categoryTitle) {
        for (Category category : categoryList) {
            if (category.getTitle().equals(categoryTitle)) {
                return category.getId();
            }
        }
        return "All";
    }


    private void populateCategoryFilter() {
        List<String> categoryTitles = new ArrayList<>();
        categoryTitles.add("Category: All"); // Thêm mục "All" với tiền tố

        for (Category category : categoryList) {
            categoryTitles.add("Category: " + category.getTitle()); // Thêm tiền tố "Category:"
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryTitles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Hiển thị "Category:" khi không thả xuống
                return super.getView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                // Xóa tiền tố "Category:" khi thả xuống
                ((TextView) view).setText(categoryTitles.get(position).replace("Category: ", ""));
                return view;
            }
        };
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }




    private List<String> getCategoryTitles() {
        List<String> titles = new ArrayList<>();
        for (Category category : categoryList) {
            titles.add(category.getTitle());
        }
        return titles;
    }


    // Load products from Firebase
    private void loadProducts() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();  // Clear danh sách trước khi thêm mới để tránh trùng lặp
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item product = dataSnapshot.getValue(Item.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                populateFilters();  // Gọi hàm này để đổ dữ liệu vào Spinner
                filterProducts(); // Filter products when data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductAdminActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Setup search view and filter logic
    private void setupSearchAndFilters() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts();
                return false;
            }
        });

        // Spinner listeners for filtering
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showViewDialog(Item product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_update_product, null);
        builder.setView(dialogView);

        EditText etProductTitle = dialogView.findViewById(R.id.etProductTitle);
        EditText etProductDescription = dialogView.findViewById(R.id.etProductDescription);
        EditText etProductPrice = dialogView.findViewById(R.id.etProductPrice);
        EditText etProductRating = dialogView.findViewById(R.id.etProductRating);
        ImageView imgProductPreview = dialogView.findViewById(R.id.imgProductPreview);
        LinearLayout sizeQuantityContainer = dialogView.findViewById(R.id.sizeQuantityContainer);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        // Đổ dữ liệu vào Spinner category nếu categoryList đã được tải
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getCategoryTitles());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Đặt giá trị hiện tại cho Spinner category
        String currentCategoryId = product.getCategoryId();
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(currentCategoryId)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        etProductTitle.setText(product.getTitle());
        etProductDescription.setText(product.getDescription());
        etProductPrice.setText(String.valueOf(product.getPrice()));
        etProductRating.setText(String.valueOf(product.getRating()));

        if (product.getSizeQuantity() != null) {
            sizeQuantityContainer.removeAllViews(); // Xóa các view cũ trước khi thêm mới
            for (Item.SizeQuantity sizeQuantity : product.getSizeQuantity()) {
                View sizeQuantityView = LayoutInflater.from(this).inflate(R.layout.item_size_quantity, sizeQuantityContainer, false);
                EditText etSize = sizeQuantityView.findViewById(R.id.etSize);
                EditText etQuantity = sizeQuantityView.findViewById(R.id.etQuantity);

                etSize.setText(String.valueOf(sizeQuantity.getSizeNumber()));
                etQuantity.setText(String.valueOf(sizeQuantity.getQuantity()));

                // Đặt các trường thành read-only
                etSize.setEnabled(false);
                etQuantity.setEnabled(false);

                sizeQuantityContainer.addView(sizeQuantityView);
            }
        }

        if (product.getPicUrl() != null && !product.getPicUrl().isEmpty()) {
            Picasso.get().load(product.getPicUrl().get(0)).into(imgProductPreview);
        }

        // Đặt các trường khác thành read-only
        etProductTitle.setEnabled(false);
        etProductDescription.setEnabled(false);
        etProductPrice.setEnabled(false);
        etProductRating.setEnabled(false);
        spinnerCategory.setEnabled(false);

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }





    private void populateFilters() {
        // Lấy giá trị cho kích thước
        Set<Integer> sizeSet = new HashSet<>();
        Set<String> statusSet = new HashSet<>();

        for (Item product : productList) {
            if (product.getSizeQuantity() != null) {
                for (Item.SizeQuantity sizeQuantity : product.getSizeQuantity()) {
                    sizeSet.add(sizeQuantity.getSizeNumber());
                }
            }

            if (product.isActive()) {
                statusSet.add("Active");
            } else {
                statusSet.add("Disable");
            }
        }

        // Chuẩn bị danh sách cho Spinner size và status
        List<Integer> sizeList = new ArrayList<>(sizeSet);
        Collections.sort(sizeList);
        sizeList.add(0, -1);

        List<String> statusList = new ArrayList<>(statusSet);
        statusList.add(0, "All");

        // Adapter cho Spinner Size
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, convertSizeListToString(sizeList)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setText(getItem(position).replace("Size: ", ""));
                return view;
            }
        };
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(sizeAdapter);

        // Adapter cho Spinner Status
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, convertStatusListToString(statusList)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setText(getItem(position).replace("Status: ", ""));
                return view;
            }
        };
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }


    // Chuyển đổi danh sách kích thước thành danh sách String có tiền tố "Size: "
    private List<String> convertSizeListToString(List<Integer> sizeList) {
        List<String> sizeWithPrefix = new ArrayList<>();
        for (Integer size : sizeList) {
            if (size == -1) {
                sizeWithPrefix.add("Size: All");
            } else {
                sizeWithPrefix.add("Size: " + size);
            }
        }
        return sizeWithPrefix;
    }

    // Chuyển đổi danh sách trạng thái thành danh sách String có tiền tố "Status: "
    private List<String> convertStatusListToString(List<String> statusList) {
        List<String> statusWithPrefix = new ArrayList<>();
        for (String status : statusList) {
            if (status.equals("All")) {
                statusWithPrefix.add("Status: All");
            } else {
                statusWithPrefix.add("Status: " + status);
            }
        }
        return statusWithPrefix;
    }




    // Filter products based on search and selected filters
    private void filterProducts() {
        String searchText = searchView.getQuery().toString().toLowerCase();
        String selectedCategoryText = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Category: All";
        String selectedSizeText = spinnerSize.getSelectedItem() != null ? spinnerSize.getSelectedItem().toString() : "Size: All";
        String selectedStatusText = spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "Status: All";

        String selectedCategoryId = getCategoryIdByTitle(selectedCategoryText.replace("Category: ", ""));
        String selectedSize = selectedSizeText.replace("Size: ", "");
        String selectedStatus = selectedStatusText.replace("Status: ", "");

        filteredProductList.clear();

        for (Item product : productList) {
            if (product == null || product.getTitle() == null) {
                continue;
            }

            boolean matchesSearch = product.getTitle().toLowerCase().contains(searchText);
            boolean matchesCategory = selectedCategoryId.equals("All") || (product.getCategoryId() != null && product.getCategoryId().equals(selectedCategoryId));
            boolean matchesSize = selectedSize.equals("All") || (product.getSizeQuantity() != null && hasSize(product, Integer.parseInt(selectedSize)));
            boolean matchesStatus = selectedStatus.equals("All") ||
                    (selectedStatus.equals("Active") && product.isActive()) ||
                    (selectedStatus.equals("Disable") && !product.isActive());

            if (matchesSearch && matchesCategory && matchesSize && matchesStatus) {
                filteredProductList.add(product);
            }
        }

        productAdapter.notifyDataSetChanged();
    }








    // Check if product has a specific size
    private boolean hasSize(Item product, int size) {
        for (Item.SizeQuantity sq : product.getSizeQuantity()) {
            if (sq.getSizeNumber() == size) {
                return true;
            }
        }
        return false;
    }

    // Show dialog for adding a new product
    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_update_product, null);
        builder.setView(dialogView);

        EditText etProductTitle = dialogView.findViewById(R.id.etProductTitle);
        EditText etProductDescription = dialogView.findViewById(R.id.etProductDescription);
        EditText etProductPrice = dialogView.findViewById(R.id.etProductPrice);
        EditText etProductRating = dialogView.findViewById(R.id.etProductRating);
        ImageView imgProductPreview = dialogView.findViewById(R.id.imgProductPreview);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        LinearLayout sizeQuantityContainer = dialogView.findViewById(R.id.sizeQuantityContainer);
        Button btnAddSizeQuantity = dialogView.findViewById(R.id.btnAddSizeQuantity);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);

        // Đổ dữ liệu vào Spinner category nếu categoryList đã được tải
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getCategoryTitles());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        btnUploadImage.setOnClickListener(v -> openImageChooser());
        btnAddSizeQuantity.setOnClickListener(v -> addSizeQuantityInput(sizeQuantityContainer));

        builder.setPositiveButton("Add", null); // Đặt null để xử lý thủ công
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (hasIncompleteFields(etProductTitle, etProductDescription, etProductPrice, etProductRating)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("You have incomplete fields. Are you sure you want to cancel?")
                        .setPositiveButton("Yes", (dialog1, which1) -> dialog.dismiss())
                        .setNegativeButton("No", null)
                        .show();
            } else {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateInputFields(etProductTitle, etProductDescription, etProductPrice, etProductRating)) {
                String title = etProductTitle.getText().toString().trim();
                String description = etProductDescription.getText().toString().trim();
                long price = Long.parseLong(etProductPrice.getText().toString().trim());
                double rating = Double.parseDouble(etProductRating.getText().toString().trim());

                if (spinnerCategory.getSelectedItem() != null) {
                    String selectedCategory = spinnerCategory.getSelectedItem().toString();
                    String categoryId = getCategoryIdByTitle(selectedCategory);

                    List<Item.SizeQuantity> sizeQuantities = getSizeQuantitiesFromContainer(sizeQuantityContainer);

                    if (imageUri != null) {
                        uploadImageToFirebase(imageUri, imgProductPreview, (imageUrl) -> {
                            Item newProduct = new Item();
                            newProduct.setTitle(title);
                            newProduct.setDescription(description);
                            newProduct.setPrice(price);
                            newProduct.setRating(rating);
                            newProduct.setActive(true);
                            newProduct.setSizeQuantity(sizeQuantities);
                            newProduct.setPicUrl(new ArrayList<>());
                            newProduct.getPicUrl().add(imageUrl);
                            newProduct.setCategoryId(categoryId);
                            saveProductToFirebase(newProduct);
                            alertDialog.dismiss();
                        });
                    } else {
                        Toast.makeText(ProductAdminActivity.this, "Please upload an image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductAdminActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProductAdminActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            }
        });
    }










    // Method to dynamically add size-quantity input fields with a remove button
    private void addSizeQuantityInput(LinearLayout container) {
        // Inflate the size-quantity layout
        View sizeQuantityView = LayoutInflater.from(this).inflate(R.layout.item_size_quantity, container, false);

        // Set up the remove button (ImageButton with delete icon)
        ImageButton btnRemove = sizeQuantityView.findViewById(R.id.btnRemoveSizeQuantity);
        btnRemove.setOnClickListener(v -> container.removeView(sizeQuantityView));

        // Add the entire size-quantity view (with "X" button) to the container
        container.addView(sizeQuantityView);
    }


    // Method to extract size-quantity values from the container
    private List<Item.SizeQuantity> getSizeQuantitiesFromContainer(LinearLayout container) {
        List<Item.SizeQuantity> sizeQuantities = new ArrayList<>();

        for (int i = 0; i < container.getChildCount(); i++) {
            View sizeQuantityView = container.getChildAt(i);

            EditText etSize = sizeQuantityView.findViewById(R.id.etSize);
            EditText etQuantity = sizeQuantityView.findViewById(R.id.etQuantity);

            String sizeText = etSize.getText().toString().trim();
            String quantityText = etQuantity.getText().toString().trim();

            // Kiểm tra nếu các trường trống, đặt lỗi và bỏ qua phần tử này
            if (sizeText.isEmpty()) {
                etSize.setError("Size cannot be empty");
                continue;
            }
            if (quantityText.isEmpty()) {
                etQuantity.setError("Quantity cannot be empty");
                continue;
            }

            try {
                int size = Integer.parseInt(sizeText);
                int quantity = Integer.parseInt(quantityText);
                sizeQuantities.add(new Item.SizeQuantity(size, quantity));
            } catch (NumberFormatException e) {
                etSize.setError("Invalid input");
                etQuantity.setError("Invalid input");
            }
        }

        return sizeQuantities;
    }



    // Open image chooser
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Truy cập ImageView trong dialog thông qua dialogView
            ImageView imgProductPreview = dialogView.findViewById(R.id.imgProductPreview);
            if (imgProductPreview != null) {
                imgProductPreview.setImageURI(imageUri); // Hiển thị ảnh được chọn
            } else {
                Toast.makeText(this, "Error: ImageView is null", Toast.LENGTH_SHORT).show();
            }
        }
    }







    // Upload image to Firebase
    private void uploadImageToFirebase(Uri imageUri, ImageView imgProductPreview, OnImageUploadCallback callback) {
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    imgProductPreview.setImageURI(imageUri); // Preview uploaded image
                    callback.onSuccess(imageUrl);
                    Toast.makeText(ProductAdminActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(ProductAdminActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    // Interface for image upload callback
    interface OnImageUploadCallback {
        void onSuccess(String imageUrl);
    }

    // Save the new product to Firebase
    private void saveProductToFirebase(Item product) {
        // Generate a unique key for the product
        String productId = productRef.push().getKey(); // This will generate a unique key
        if (productId != null) {
            product.setId(productId); // Set the generated ID to the product
            productRef.child(productId).setValue(product)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(ProductAdminActivity.this, "Product added", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(ProductAdminActivity.this, "Failed to add product", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(ProductAdminActivity.this, "Failed to generate product ID", Toast.LENGTH_SHORT).show();
        }
    }

// Show update dialog
private void showUpdateDialog(Item product) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    LayoutInflater inflater = this.getLayoutInflater();
    dialogView = inflater.inflate(R.layout.dialog_update_product, null);
    builder.setView(dialogView);

    EditText etProductTitle = dialogView.findViewById(R.id.etProductTitle);
    EditText etProductDescription = dialogView.findViewById(R.id.etProductDescription);
    EditText etProductPrice = dialogView.findViewById(R.id.etProductPrice);
    EditText etProductRating = dialogView.findViewById(R.id.etProductRating);
    ImageView imgProductPreview = dialogView.findViewById(R.id.imgProductPreview);
    Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
    LinearLayout sizeQuantityContainer = dialogView.findViewById(R.id.sizeQuantityContainer);
    Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);
    Button btnAddSizeQuantity = dialogView.findViewById(R.id.btnAddSizeQuantity);

    // Đổ dữ liệu vào Spinner category nếu categoryList đã được tải
    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getCategoryTitles());
    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerCategory.setAdapter(categoryAdapter);

    // Đặt giá trị hiện tại cho Spinner category
    String currentCategoryId = product.getCategoryId();
    for (int i = 0; i < categoryList.size(); i++) {
        if (categoryList.get(i).getId().equals(currentCategoryId)) {
            spinnerCategory.setSelection(i);
            break;
        }
    }

    etProductTitle.setText(product.getTitle());
    etProductDescription.setText(product.getDescription());
    etProductPrice.setText(String.valueOf(product.getPrice()));
    etProductRating.setText(String.valueOf(product.getRating()));

    if (product.getPicUrl() != null && !product.getPicUrl().isEmpty()) {
        Picasso.get().load(product.getPicUrl().get(0)).into(imgProductPreview);
    }

    if (product.getSizeQuantity() != null) {
        for (Item.SizeQuantity sizeQuantity : product.getSizeQuantity()) {
            View sizeQuantityView = LayoutInflater.from(this).inflate(R.layout.item_size_quantity, sizeQuantityContainer, false);
            EditText etSize = sizeQuantityView.findViewById(R.id.etSize);
            EditText etQuantity = sizeQuantityView.findViewById(R.id.etQuantity);
            ImageButton btnRemove = sizeQuantityView.findViewById(R.id.btnRemoveSizeQuantity);

            etSize.setText(String.valueOf(sizeQuantity.getSizeNumber()));
            etQuantity.setText(String.valueOf(sizeQuantity.getQuantity()));
            btnRemove.setOnClickListener(v -> sizeQuantityContainer.removeView(sizeQuantityView));

            sizeQuantityContainer.addView(sizeQuantityView);
        }
    }

    btnAddSizeQuantity.setOnClickListener(v -> addSizeQuantityInput(sizeQuantityContainer));
    btnUploadImage.setOnClickListener(v -> openImageChooser());

    builder.setPositiveButton("Update", null); // Đặt null để xử lý thủ công
    builder.setNegativeButton("Cancel", (dialog, which) -> {
        if (hasIncompleteFields(etProductTitle, etProductDescription, etProductPrice, etProductRating)) {
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("You have incomplete fields. Are you sure you want to cancel?")
                    .setPositiveButton("Yes", (dialog1, which1) -> dialog.dismiss())
                    .setNegativeButton("No", null)
                    .show();
        } else {
            dialog.dismiss();
        }
    });

    AlertDialog alertDialog = builder.create();
    alertDialog.show();

    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        if (validateInputFields(etProductTitle, etProductDescription, etProductPrice, etProductRating)) {
            product.setTitle(etProductTitle.getText().toString().trim());
            product.setDescription(etProductDescription.getText().toString().trim());
            product.setPrice(Long.parseLong(etProductPrice.getText().toString().trim()));
            product.setRating(Double.parseDouble(etProductRating.getText().toString().trim()));

            List<Item.SizeQuantity> sizeQuantities = getSizeQuantitiesFromContainer(sizeQuantityContainer);
            product.setSizeQuantity(sizeQuantities);

            String selectedCategoryTitle = spinnerCategory.getSelectedItem().toString();
            String selectedCategoryId = getCategoryIdByTitle(selectedCategoryTitle);
            product.setCategoryId(selectedCategoryId);

            if (imageUri != null) {
                uploadImageToFirebase(imageUri, imgProductPreview, (imageUrl) -> {
                    product.getPicUrl().clear();
                    product.getPicUrl().add(imageUrl);
                    updateProductInFirebase(product);
                    alertDialog.dismiss();
                });
            } else {
                updateProductInFirebase(product);
                alertDialog.dismiss();
            }
        } else {
            Toast.makeText(ProductAdminActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
        }
    });
}

    private boolean hasIncompleteFields(EditText... fields) {
        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }








    private void updateProductInFirebase(Item product) {
        // Lưu lại trạng thái bộ lọc hiện tại
        currentStatusFilter = spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "Status: All";
        currentSizeFilter = spinnerSize.getSelectedItem() != null ? spinnerSize.getSelectedItem().toString() : "Size: All";
        currentCategoryFilter = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "Category: All";
        currentSearchQuery = searchView.getQuery().toString().toLowerCase();

        // Cập nhật sản phẩm trong Firebase
        productRef.child(product.getId()).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProductAdminActivity.this, "Product updated", Toast.LENGTH_SHORT).show();

                    // Update product in local list
                    for (int i = 0; i < productList.size(); i++) {
                        if (productList.get(i).getId().equals(product.getId())) {
                            productList.set(i, product);
                            break;
                        }
                    }

                    // Cập nhật lại giao diện với bộ lọc hiện tại
                    restoreFilters();
                    filterProducts();
                })
                .addOnFailureListener(e -> Toast.makeText(ProductAdminActivity.this, "Failed to update product", Toast.LENGTH_SHORT).show());
    }

    // Phương thức khôi phục lại bộ lọc sau khi cập nhật
    private void restoreFilters() {
        setSpinnerSelection(spinnerStatus, currentStatusFilter);
        setSpinnerSelection(spinnerSize, currentSizeFilter);
        setSpinnerSelection(spinnerCategory, currentCategoryFilter);
        searchView.setQuery(currentSearchQuery, false);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private boolean validateInputFields(EditText... fields) {
        boolean isValid = true;
        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                field.setError("This field cannot be empty");
                isValid = false;
            } else {
                field.setError(null); // Clear any previous error
            }
        }
        return isValid;
    }



    // Show product details
    private void showProductDetails(Item product) {
        String details = "Title: " + product.getTitle() + "\n" +
                "Description: " + product.getDescription() + "\n" +
                "Price: " + product.getPrice() + "\n" +
                "Rating: " + product.getRating() + "\n" +
                "Active: " + (product.isActive() ? "Yes" : "No");

        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }
}
