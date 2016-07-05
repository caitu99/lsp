package com.caitu99.lsp.model.parser;


import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.ParserStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParserContext {

    private String id = UUID.randomUUID().toString().replace("-", "");
    private int userId;
    private String account;
    private Collection<MailSrc> mailSrcs;
    private AtomicInteger mailSrcCount = new AtomicInteger(0);
    private Collection<MailSrc> unparsedMailSrcs = new Vector<>();

    private ParserStatus status = ParserStatus.HEAD;

    private List<Bill> bills = new Vector<>();

    private String redisKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Collection<MailSrc> getMailSrcs() {
        return mailSrcs;
    }

    public void setMailSrcs(Collection<MailSrc> mailSrcs) {
        this.mailSrcs = mailSrcs;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public AtomicInteger getMailSrcCount() {
        return mailSrcCount;
    }

    public void setMailSrcCount(AtomicInteger mailSrcCount) {
        this.mailSrcCount = mailSrcCount;
    }

    public ParserStatus getStatus() {
        return status;
    }

    public void setStatus(ParserStatus status) {
        this.status = status;
    }

    public Collection<MailSrc> getUnparsedMailSrcs() {
        return unparsedMailSrcs;
    }

    public void setUnparsedMailSrcs(Collection<MailSrc> unparsedMailSrcs) {
        this.unparsedMailSrcs = unparsedMailSrcs;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    @Override
    public String toString() {
        return "ParserContext{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", account='" + account + '\'' +
                '}';
    }
}
