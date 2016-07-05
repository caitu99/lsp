package com.caitu99.lsp.model.spider;

public class Envelope {


    private String id;
    private String from;
    private String to;
    private String subject;
    private String sentDate;
    private String receiveDate;

    public Envelope() {
        super();
    }

    public Envelope(String id, String from, String to, String subject,
                    String sentDate, String receiveDate) {
        super();
        this.id = id;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.sentDate = sentDate;
        this.receiveDate = receiveDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }
}
