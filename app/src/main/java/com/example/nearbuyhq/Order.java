package com.example.nearbuyhq;

public class Order {
    private String orderId, customerName, status, orderDate;
    private double orderTotal;

    public Order(String orderId, String customerName, String status,
                 double orderTotal, String orderDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.status = status;
        this.orderTotal = orderTotal;
        this.orderDate = orderDate;
    }

    public String getOrderId()      { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getStatus()       { return status; }
    public double getOrderTotal()   { return orderTotal; }
    public String getOrderDate()    { return orderDate; }
}