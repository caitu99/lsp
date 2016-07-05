package com.caitu99.lsp.model.spider.hotmail;

/**
 * Created by Lion on 2015/11/19 0019.
 */
public class HotmailItem implements Comparable<HotmailItem> {
    private String mailId;
    private String mailCode;
    private String timeLong;
    private String title;
    private String sender;
    private String sendCode;
    private String otherCode;
    private String timeStr;
    private String body;
    private String size;

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public String getMailCode() {
        return mailCode;
    }

    public void setMailCode(String mailCode) {
        this.mailCode = mailCode;
    }

    public String getTimeLong() {
        return timeLong;
    }

    public void setTimeLong(String timeLong) {
        this.timeLong = timeLong;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSendCode() {
        return sendCode;
    }

    public void setSendCode(String sendCode) {
        this.sendCode = sendCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getOtherCode() {
        return otherCode;
    }

    public void setOtherCode(String otherCode) {
        this.otherCode = otherCode;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }


    @Override
    public String toString() {
        return "HotmailItem{" +
                "mailId='" + mailId + '\'' +
                ", mailCode='" + mailCode + '\'' +
                ", timeLong='" + timeLong + '\'' +
                ", title='" + title + '\'' +
                ", sender='" + sender + '\'' +
                ", sendCode='" + sendCode + '\'' +
                ", otherCode='" + otherCode + '\'' +
                ", timeStr='" + timeStr + '\'' +
                ", body='" + body + '\'' +
                ", size='" + size + '\'' +
                '}';
    }

    @Override
    public int compareTo(HotmailItem o) {
        Long timeThis = Long.valueOf(this.getTimeLong());
        Long oTime = Long.valueOf(o.getTimeLong());
        if (timeThis > oTime) {
            return 1;
        } else if (timeThis.equals(oTime)) {
            return 0;
        } else {
            return -1;
        }

    }
}
