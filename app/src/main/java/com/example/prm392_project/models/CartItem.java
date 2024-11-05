package com.example.prm392_project.models;

/**
 * NOTE: productId in CartItem != productId in Item (Product). Due to database design, a single
 * product may have multiple sizes, and therefore has multiple quantity, in practice it is usually
 * stored as two separated products (and have two ProductId). However, ThangHQ does not want to
 * re-design database as of now, and I decide to create an ID for CartID = {ItemID}:{Size}.
 */
public class CartItem {

    private int quantity;
    private double price;
    private String name;
    private String productImg;
    private String productId;
    private int size;

    public CartItem(int quantity, double price, String title, String productImg, String productId, int size) {
        this.quantity = quantity;
        this.price = price;
        this.name = title;
        this.productImg = productImg;
        this.productId = productId;
        this.size = size;
    }

    /**
     * Create an instance of CartItem from Item (Product).
     * Note that: ProductImg in cart will default to the first picture of product
     *
     * @param item
     */
    public CartItem(Item item, int size) {
        quantity = 1;
        price = item.getPrice();
        name = item.getTitle();
        productImg = item.getPicUrl().get(0);
        productId = item.getId();
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getProductImg() {
        return productImg;
    }

    public String getProductId() {
        return productId;
    }

    /**
     * Be aware that CartItemId != ItemId
     * @return CartItem's id.
     */
    public String getCartItemId() {
        return productId + ":" + size;
    }

//    public void setProductId(String productId) {
//        this.productId = productId;
//    }

//    public void setProductImg(String productImg) {
//        this.productImg = productImg;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
