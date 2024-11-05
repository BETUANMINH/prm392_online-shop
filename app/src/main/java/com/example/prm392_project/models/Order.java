package com.example.prm392_project.models;

public class Order {
    private String id;
    private String total;
    private long quantity;
    private String productImg;
    private String date;
    private String status;

    public Order() {}



    public Order(String id, String total, long quantity, String date, String productImg, String status) {
        this.id = id;
        this.total = total;
        this.quantity = quantity;
        this.productImg = productImg;
        this.date = date;
        this.status = status;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }
}
