package com.example.nearbuyhq.reports;

public class Report {
    private String id;
    private String type;
    private String subject;
    private String description;
    private String status;
    private String customerName;
    private String orderRef;

    public Report(String id, String type, String subject, String description,
                  String status, String customerName, String orderRef) {
        this.id = id;
        this.type = type;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.customerName = customerName;
        this.orderRef = orderRef;
    }

    // Backward-compat constructor (no customerName/orderRef)
    public Report(String id, String type, String subject, String description, String status) {
        this(id, type, subject, description, status, "", "");
    }

    public String getId()           { return id; }
    public String getType()         { return type; }
    public String getSubject()      { return subject; }
    public String getDescription()  { return description; }
    public String getStatus()       { return status; }
    public String getCustomerName() { return customerName == null ? "" : customerName; }
    public String getOrderRef()     { return orderRef == null ? "" : orderRef; }
    public void setStatus(String status) { this.status = status; }
}
