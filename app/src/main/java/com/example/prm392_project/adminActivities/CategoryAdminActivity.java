package com.example.prm392_project.adminActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_project.R;
import com.example.prm392_project.adapter.CategoryAdapter;
import com.example.prm392_project.models.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryAdminActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private Button btnAddCategory;
    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Category> categoryList = new ArrayList<>();
    private ArrayList<Category> filteredList = new ArrayList<>(); // Danh sách đã lọc
    private Uri imageUri; // Lưu trữ URI của ảnh đã chọn
    private ImageView imgPreview; // ImageView để hiển thị preview ảnh
    private StorageReference storageRef; // Firebase Storage Reference
    private DatabaseReference categoryRef;
    private SearchView searchView; // Thêm SearchView

    private static final int REQUEST_CODE_PICK_IMAGE = 100; // Mã yêu cầu cho việc chọn ảnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_admin);

        btnAddCategory = findViewById(R.id.btnAddCategory);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        searchView = findViewById(R.id.searchView); // Khởi tạo SearchView
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Thiết lập Firebase Database và Storage
        categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        storageRef = FirebaseStorage.getInstance().getReference("CategoryImages");

        // Thiết lập adapter cho RecyclerView
        categoryAdapter = new CategoryAdapter(this, categoryList, this);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Load dữ liệu danh mục từ Firebase
        readCategories();

        // Sự kiện khi bấm nút "Add Category"
        btnAddCategory.setOnClickListener(v -> {
            showAddCategoryDialog();
        });

        // Thiết lập SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCategories(newText);
                return true;
            }
        });
    }

    // Hiển thị dialog thêm danh mục mới
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        imgPreview = view.findViewById(R.id.imgPreview); // ImageView để hiển thị preview ảnh
        EditText etCategoryTitle = view.findViewById(R.id.etCategoryTitle);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);
        Button btnAddCategoryConfirm = view.findViewById(R.id.btnAddCategoryConfirm);

        // Sự kiện khi bấm nút "Upload Image"
        btnUploadImage.setOnClickListener(v -> {
            openImagePicker();
        });

        // Sự kiện khi bấm nút "Add Category"
        btnAddCategoryConfirm.setOnClickListener(v -> {
            String title = etCategoryTitle.getText().toString().trim();
            if (!title.isEmpty() && imageUri != null) {
                uploadImageAndAddCategory(title);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter title and select an image", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // Mở thư viện ảnh để chọn ảnh
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // Xử lý kết quả sau khi người dùng chọn ảnh từ thư viện
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Lưu URI của ảnh đã chọn
            imgPreview.setImageURI(imageUri); // Hiển thị preview trong ImageView
        }
    }

    // Tải ảnh lên Firebase và thêm danh mục mới
    private void uploadImageAndAddCategory(String title) {
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            createCategory(title, imageUrl); // Tạo danh mục mới với URL ảnh
        })).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }


// Tạo danh mục mới trong Firebase
    private void createCategory(String title, String picUrl) {
        // Tính toán ID mới bằng cách lấy độ dài của danh sách
        String newId = String.valueOf(categoryList.size() );

        // Tạo danh mục mới với ID mới
        Category newCategory = new Category(newId, title, picUrl);
        categoryRef.child(newId).setValue(newCategory)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CategoryAdminActivity.this, "Category added", Toast.LENGTH_SHORT).show();
                    readCategories();
                })
                .addOnFailureListener(e -> Toast.makeText(CategoryAdminActivity.this, "Failed to add category", Toast.LENGTH_SHORT).show());
    }


    // Đọc danh mục từ Firebase và cập nhật RecyclerView
    private void readCategories() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.getKey();
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String picUrl = dataSnapshot.child("picUrl").getValue(String.class);
                    Category category = new Category(id, title, picUrl);
                    categoryList.add(category);
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryAdminActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị hộp thoại chỉnh sửa với dữ liệu có sẵn
    @Override
    public void onEditCategoryClick(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        imgPreview = view.findViewById(R.id.imgPreview); // ImageView để hiển thị preview ảnh
        EditText etCategoryTitle = view.findViewById(R.id.etCategoryTitle);
        Button btnUploadImage = view.findViewById(R.id.btnUploadImage);
        Button btnAddCategoryConfirm = view.findViewById(R.id.btnAddCategoryConfirm);

        // Hiển thị thông tin danh mục cũ
        etCategoryTitle.setText(category.getTitle());
        Picasso.get().load(category.getPicUrl()).into(imgPreview);

        // Sự kiện khi bấm nút "Upload Image"
        btnUploadImage.setOnClickListener(v -> {
            openImagePicker();
        });

        // Sự kiện khi bấm nút "Confirm Edit"
        btnAddCategoryConfirm.setText("Confirm Edit");
        btnAddCategoryConfirm.setOnClickListener(v -> {
            String updatedTitle = etCategoryTitle.getText().toString().trim();
            if (!updatedTitle.isEmpty()) {
                // Cập nhật danh mục trong Firebase
                updateCategory(category.getId(), updatedTitle, category.getPicUrl());
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter a valid title", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // Cập nhật danh mục trong Firebase
    private void updateCategory(String id, String title, String picUrl) {
        Category updatedCategory = new Category(id, title, picUrl);
        categoryRef.child(id).setValue(updatedCategory)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
                    readCategories();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show());
    }

    // Hiển thị hộp thoại xác nhận trước khi xóa
    @Override
    public void onDeleteCategoryClick(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteCategory(category.getId());
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Xóa danh mục trong Firebase
    private void deleteCategory(String id) {
        categoryRef.child(id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                    readCategories();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show());
    }

    // Hàm để lọc danh mục dựa trên input từ SearchView
    private void filterCategories(String text) {
        filteredList.clear();
        for (Category category : categoryList) {
            if (category.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(category);
            }
        }
        categoryAdapter.updateList(filteredList); // Cập nhật danh sách trong adapter
    }
}
