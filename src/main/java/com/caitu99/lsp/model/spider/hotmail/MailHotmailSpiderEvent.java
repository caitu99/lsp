package com.caitu99.lsp.model.spider.hotmail;

import com.alibaba.fastjson.annotation.JSONField;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Lion on 2015/11/14 0014.
 */
public class MailHotmailSpiderEvent extends MailSpiderEvent {

    private MailHotmailSpiderState state = MailHotmailSpiderState.NONE;

    private String refurlforlogin;
    private String login_url;
    private String PPFT;
    private String passport;
    private String location;
    private String login_next;
    private String hotmail_sessionId;
    private String hotmail_userid;
    private String verifyUlr;
    private boolean needvir;
    private String verifyimgurl;
    private String hid;
    private Integer cnt = 0;
    private Set<HotmailItem> mailSet = new HashSet<>();
    private LinkedHashMap<String, HotmailItem> mailMap = new LinkedHashMap<>();
    private HotMail hotmail = new HotMail();
    private String changviewurl;
    private String changviewurl_next;
    private boolean hasChangeView = false;  //

    public boolean isHasChangeView() {
        return hasChangeView;
    }

    public void setHasChangeView(boolean hasChangeView) {
        this.hasChangeView = hasChangeView;
    }

    public String getChangviewurl_next() {
        return changviewurl_next;
    }

    public void setChangviewurl_next(String changviewurl_next) {
        this.changviewurl_next = changviewurl_next;
    }

    public String getChangviewurl() {
        return changviewurl;
    }

    public void setChangviewurl(String changviewurl) {
        this.changviewurl = changviewurl;
    }

    private ConcurrentLinkedDeque<HotmailItem> hotmailItems = new ConcurrentLinkedDeque<>();


    public MailHotmailSpiderEvent() {
    }

    public MailHotmailSpiderEvent(String userid, DeferredResult<Object> deferredResult, HttpServletRequest request) {
        super(userid, deferredResult, request);
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public MailHotmailSpiderState getState() {
        return state;
    }

    public void setState(MailHotmailSpiderState state) {
        this.state = state;
    }


    public String getLogin_url() {
        return login_url;
    }

    public void setLogin_url(String login_url) {
        this.login_url = login_url;
    }

    public String getPPFT() {
        return PPFT;
    }

    public void setPPFT(String PPFT) {
        this.PPFT = PPFT;
    }

    public String getRefurlforlogin() {
        return refurlforlogin;
    }

    public void setRefurlforlogin(String refurlforlogin) {
        this.refurlforlogin = refurlforlogin;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLogin_next() {
        return login_next;
    }

    public void setLogin_next(String login_next) {
        this.login_next = login_next;
    }

    public String getHotmail_sessionId() {
        return hotmail_sessionId;
    }

    public void setHotmail_sessionId(String hotmail_sessionId) {
        this.hotmail_sessionId = hotmail_sessionId;
    }

    public String getHotmail_userid() {
        return hotmail_userid;
    }

    public void setHotmail_userid(String hotmail_userid) {
        this.hotmail_userid = hotmail_userid;
    }

    public Set<HotmailItem> getMailSet() {
        return mailSet;
    }

    public void setMailSet(Set<HotmailItem> mailSet) {
        this.mailSet = mailSet;
    }

    public HotMail getHotmail() {
        return hotmail;
    }

    public void setHotmail(HotMail hotmail) {
        this.hotmail = hotmail;
    }

    public LinkedHashMap<String, HotmailItem> getMailMap() {
        return mailMap;
    }

    public void setMailMap(LinkedHashMap<String, HotmailItem> mailMap) {
        this.mailMap = mailMap;
    }

    public ConcurrentLinkedDeque<HotmailItem> getHotmailItems() {
        return hotmailItems;
    }

    public void setHotmailItems(ConcurrentLinkedDeque<HotmailItem> hotmailItems) {
        this.hotmailItems = hotmailItems;
    }

    public String getVerifyUlr() {
        return verifyUlr;
    }

    public void setVerifyUlr(String verifyUlr) {
        this.verifyUlr = verifyUlr;
    }

    public boolean isNeedvir() {
        return needvir;
    }

    public void setNeedvir(boolean needvir) {
        this.needvir = needvir;
    }

    public String getVerifyimgurl() {
        return verifyimgurl;
    }

    public void setVerifyimgurl(String verifyimgurl) {
        this.verifyimgurl = verifyimgurl;
    }

    public String getHid() {
        return hid;
    }

    public void setHid(String hid) {
        this.hid = hid;
    }

    @JSONField(deserialize = false)
    public void setException(Exception exception) {
        this.setState(MailHotmailSpiderState.ERROR);
        this.exception = exception;
    }
}
