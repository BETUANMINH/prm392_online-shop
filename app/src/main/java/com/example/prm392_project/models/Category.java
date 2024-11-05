package com.example.prm392_project.models;

public class Category {
    private String id;
    private String title;
    private String picUrl;

    // Default constructor required for Firebase
    public Category() {}

    public Category(String id, String title, String picUrl) {
        this.id = id;
        this.title = title;
        this.picUrl = picUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
