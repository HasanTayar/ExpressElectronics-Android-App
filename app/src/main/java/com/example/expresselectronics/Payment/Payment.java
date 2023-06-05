package com.example.expresselectronics.Payment;

public class Payment {
    private String id;
    private String userId;
    private double amount;
    private String orderId;
    private String date;

    public Payment() {
    }

    public Payment(String id, String userId, double amount, String orderId, String date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.orderId = orderId;
        this.date = date;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

