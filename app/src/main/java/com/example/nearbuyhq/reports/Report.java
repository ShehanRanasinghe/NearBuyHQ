package com.example.nearbuyhq.reports;

public class Report {
    private String id;
    private String type;
    private String subject;
    private String description;
    private String status;

    public Report(String id, String type, String subject, String description, String status) {
        this.id = id;
        this.type = type;
        this.subject = subject;
        this.description = description;
        this.status = status;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

