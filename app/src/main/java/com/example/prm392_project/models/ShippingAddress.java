package com.example.prm392_project.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.UUID;

public class ShippingAddress implements Parcelable {
    private String id;
    private String userId;
    private String streetAddress;
    private String city;
    private String state;
    private boolean isDefault;
    private String recipientInfo;

    public ShippingAddress() {}

    public ShippingAddress(String id, String userId, String streetAddress, String city, String state, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.isDefault = isDefault;
    }

    protected ShippingAddress(Parcel in) {
        id = in.readString();
        userId = in.readString();
        streetAddress = in.readString();
        city = in.readString();
        state = in.readString();
        isDefault = in.readByte() != 0;
        recipientInfo = in.readString();
    }

    public static final Creator<ShippingAddress> CREATOR = new Creator<ShippingAddress>() {
        @Override
        public ShippingAddress createFromParcel(Parcel in) {
            return new ShippingAddress(in);
        }

        @Override
        public ShippingAddress[] newArray(int size) {
            return new ShippingAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(streetAddress);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeByte((byte) (isDefault ? 1 : 0));
        dest.writeString(recipientInfo);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getRecipientInfo() {
        return recipientInfo;
    }

    public void setRecipientInfo(String recipientInfo) {
        this.recipientInfo = recipientInfo;
    }
} 