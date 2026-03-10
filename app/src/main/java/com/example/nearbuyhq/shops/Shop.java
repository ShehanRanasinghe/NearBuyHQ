package com.example.nearbuyhq.shops;

public class Shop {
    private String id;
    private String name;
    private String owner;
    private String location;
    private String category;
    private String status;

    public Shop(String id, String name, String owner, String location, String category, String status) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.category = category;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}

