package com.example.expresselectronics.Order;

import com.example.expresselectronics.Product.Product;


import java.util.List;

public class Order {
    private String id;
    private List<Product> orderedItems;
    private String date;
    private double totalPrice;

    // Empty constructor required for Firestore
    public Order() {
    }

    public Order(String id, List<Product> orderedItems, String date, double totalPrice) {
        this.id = id;
        this.orderedItems = orderedItems;
        this.date = date;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Product> getOrderedItems() {
        return orderedItems;
    }

    public void setOrderedItems(List<Product> orderedItems) {
        this.orderedItems = orderedItems;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}

