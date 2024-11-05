package com.example.prm392_project.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Item implements Parcelable {
    private String id;
    private String description;
    private List<String> picUrl;
    private Long price;
    private double rating;
    private List<SizeQuantity> sizeQuantity;
    private String title;
    private boolean active;
    private String categoryId; // Thêm trường categoryId

    // Inner class for size and quantity
    public static class SizeQuantity implements Parcelable {
        private int sizeNumber;
        private int quantity;

        public SizeQuantity() {}

        public SizeQuantity(int sizeNumber, int quantity) {
            this.sizeNumber = sizeNumber;
            this.quantity = quantity;
        }

        protected SizeQuantity(Parcel in) {
            sizeNumber = in.readInt();
            quantity = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(sizeNumber);
            dest.writeInt(quantity);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SizeQuantity> CREATOR = new Creator<SizeQuantity>() {
            @Override
            public SizeQuantity createFromParcel(Parcel in) {
                return new SizeQuantity(in);
            }

            @Override
            public SizeQuantity[] newArray(int size) {
                return new SizeQuantity[size];
            }
        };

        // Getters and Setters
        public int getSizeNumber() {
            return sizeNumber;
        }

        public void setSizeNumber(int sizeNumber) {
            this.sizeNumber = sizeNumber;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // Constructors
    public Item() {
        this.active = true; // Mặc định sản phẩm sẽ active khi tạo mới
    }

    public Item(String id, String description, List<String> picUrl, Long price,
                double rating, List<SizeQuantity> sizeQuantity, String title, boolean active, String categoryId) {
        this.id = id;
        this.description = description;
        this.picUrl = picUrl;
        this.price = price;
        this.rating = rating;
        this.sizeQuantity = sizeQuantity;
        this.title = title;
        this.active = active;
        this.categoryId = categoryId; // Set categoryId
    }

    protected Item(Parcel in) {
        id = in.readString();
        description = in.readString();
        picUrl = in.createStringArrayList();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readLong();
        }
        rating = in.readDouble();
        sizeQuantity = new ArrayList<>();
        in.readTypedList(sizeQuantity, SizeQuantity.CREATOR);
        title = in.readString();
        active = in.readByte() != 0;
        categoryId = in.readString(); // Đọc giá trị categoryId từ Parcel
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeStringList(picUrl);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(price);
        }
        dest.writeDouble(rating);
        dest.writeTypedList(sizeQuantity);
        dest.writeString(title);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeString(categoryId); // Ghi giá trị categoryId vào Parcel
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    // Getters and Setters
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(List<String> picUrl) {
        this.picUrl = picUrl;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<SizeQuantity> getSizeQuantity() {
        return sizeQuantity;
    }

    public void setSizeQuantity(List<SizeQuantity> sizeQuantity) {
        this.sizeQuantity = sizeQuantity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
