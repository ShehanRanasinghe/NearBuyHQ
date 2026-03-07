package com.example.nearbuyhq;

public class Deal {
    private String id;
    private String title;
    private String shopName;
    private String discount;
    private String validity;

    public Deal(String id, String title, String shopName, String discount, String validity) {
        this.id = id;
        this.title = title;
        this.shopName = shopName;
        this.discount = discount;
        this.validity = validity;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getShopName() { return shopName; }
    public String getDiscount() { return discount; }
    public String getValidity() { return validity; }
}

