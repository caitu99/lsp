package com.caitu99.lsp.model.spider;

import java.util.Date;
import java.util.UUID;

public class MailSrc {

    private String id = UUID.randomUUID().toString().replace("-", "");

    private String title;

    private String body;

    private String bank;

    private Date date;

    private String from;

    /**
	 * @return the srcId
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the srcId to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "MailSrc{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", from='" + from + '\'' +
                ", date=" + date +
                '}';
    }
}
