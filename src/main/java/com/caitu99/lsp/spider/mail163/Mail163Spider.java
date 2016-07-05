/*
 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.spider.mail163;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.parser.BillResult;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.model.spider.SpiderResult;
import com.caitu99.lsp.model.spider.mail163.Mail163SpiderEvent;
import com.caitu99.lsp.model.spider.mail163.Mail163SpiderState;
import com.caitu99.lsp.parser.ParserReactor;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailBodySpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.spider.mailqq.MailQQSpider;
import com.caitu99.lsp.utils.cookie.CookieHelper;
import com.caitu99.lsp.utils.cookie.HttpCookieEx;

import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: sdfsdf
 * @date 2015年10月26日 下午4:28:45
 * @Copyright (c) 2015-2020 by caitu99
 */
public class Mail163Spider extends AbstractMailSpider {
    private static final Logger logger = LoggerFactory.getLogger(MailQQSpider.class);

    private static final String cookieStr = "Coremail.sid=%s; Path=/";
    private static final String loginUrl = "https://mail.163.com/entry/cgi/ntesdoor?df=mail163_letter&from=web&funcid=loginone&iframe=1&language=-1&passtype=1&product=mail163&net=c&style=-1&race=46_168_54_bj&uid=%s@163.com";
    private static final String reloginUrl = "https://reg.163.com/login.jsp?username=%s&url=http://entry.mail.163.com/coremail/fcg/ntesdoor2";
    private static final String mailListUrl = "http://mail.163.com/js6/s?sid=%s&func=mbox:listMessages&mbox_folder_enter=1";
    private static final String mailUrl = "http://mail.163.com/js6/read/readhtml.jsp?mid=%s&font=15&color=064977";
    private static final String getidUrl = "https://reg.163.com/services/getid";
    private static final String getImgUrl = "https://reg.163.com/services/getimg?id=%s";
    private static final String vfyUrl = "https://reg.163.com/services/checkcode?filledVerifyID=%s&sysVerifyID=%s&isLoginException=1";

    private static final Pattern loginDownPattern = Pattern.compile("(?<=top.location.href = \").*(?=;</)");
    private static final int MAX_MAIL_SIZE = 200;


    public Mail163Spider(MailBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    public static List<Envelope> solveDateAndToEnvelope(String envelope) {
        Pattern pattern = Pattern.compile("new Date\\(([0-9,]+)\\)");
        Matcher match = pattern.matcher(envelope);
        StringBuffer sb = new StringBuffer();
        while (match.find()) {
            match.appendReplacement(sb, "'" + match.group(1) + "'");
        }
        match.appendTail(sb);
        List<Envelope> list = new ArrayList<Envelope>();
        String jsonStr = sb.toString();
        JSONObject jsonObj = JSONObject.parseObject(jsonStr);
        JSONArray jsonArr = jsonObj.getJSONArray("var");
        if (jsonArr != null) {
            for (int i = 0; i < jsonArr.size(); i++) {
                jsonObj = jsonArr.getJSONObject(i);
                list.add(genEnvelopeFromjsonBean(jsonObj));
            }
        }
        return list;
    }

    public static Envelope genEnvelopeFromjsonBean(JSONObject jsonObj) {
        Envelope curEnvelope = new Envelope();
        curEnvelope.setId(jsonObj.getString("id"));
        curEnvelope.setFrom(jsonObj.getString("from"));
        curEnvelope.setTo(jsonObj.getString("to"));
        curEnvelope.setSubject(jsonObj.getString("subject"));
        //加一个月时间
        String sendDate = jsonObj.getString("sentDate");
        sendDate = getLongTime(sendDate);
        String receivedDate = jsonObj.getString("receivedDate");
        receivedDate = getLongTime(receivedDate);
        curEnvelope.setSentDate(sendDate);
        curEnvelope.setReceiveDate(receivedDate);
        return curEnvelope;
    }

    /**
     * @date 2015年11月9日 下午3:07:10
     * @author yukf
     */
    public static String getLongTime(String sendDate) {
        String[] time = sendDate.split(",");
        Calendar cal = new Calendar.Builder().setDate(Integer.valueOf(time[0]), Integer.valueOf(time[1]), Integer.valueOf(time[2]))
                .setTimeOfDay(Integer.valueOf(time[3]), Integer.valueOf(time[4]), Integer.valueOf(time[5])).build();
        return "" + (cal.getTime().getTime() / 1000);
    }

    @Override
    public void onEvent(MailSpiderEvent event) {
        Mail163SpiderEvent l63Event = (Mail163SpiderEvent) event;
        try {
            switch (l63Event.getState()) {
                case LOGIN:
                    loginUp(l63Event);
                    break;
                case RELOGIN:
                    reloginUp(l63Event);
                    break;
                case GETID:
                    getidUp(l63Event);
                    break;
                case GETIMG:
                    getImgUp(l63Event);
                    break;
                case VFY:
                    vfyUp(l63Event);
                    break;
                case NEXT:
                    nextUp(l63Event);
                    break;
                case ENTRY:
                    entryUp(l63Event);
                    break;
                case MAILLIST:
                    mailListUp(l63Event);
                    break;
                case TASKQUEUE:
                    taskQueue(l63Event);
                    break;
                case MAIL:
                    mailUp(l63Event);
                    break;
                case PARSETASKQUEUE:
                    parseTaskQueue(l63Event);
                    break;
                case PARSE:
                    parseMail(l63Event);
                    break;
                case ERROR:
                    errorHandle(l63Event);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            l63Event.setException(e);
            errorHandle(l63Event);
        }

    }

    @Override
    protected void setHeader(String uriStr, HttpMessage httpGet, MailSpiderEvent event) {
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", USERAGENT_CHROME);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    /**
     * parse mail
     *
     * @date 2015年10月30日 上午11:54:35
     * @author yukf
     */
    private void parseMail(Mail163SpiderEvent event) {
        logger.debug("do parse mail {}", event.getId());
        try {
            ParserContext context = new ParserContext();
            context.setUserId(Integer.parseInt(event.getUserid()));
            String account = event.getAccount();
            if (!account.contains("@")) {
                account = account + "@163.com";
            }
            context.setAccount(account);
            context.setMailSrcs(event.getMailSrcs());
            context.setRedisKey(Constant.mail163ResultKey);
            ParserReactor.getInstance().process(context);
        }
        finally {
            String key = String.format(Constant.MAIL163TASKQUEUE, event.getAccount());
            redis.del(key);
        }

//        logger.debug("do parse mail {}", event.getId());
//        MailParser parser = MailParser.getInstance();
//        Map<String, List<UserCardVo>> result = parser.execute(event.getMailSrcs());
//        SpiderResult spiderResult = createSpiderResult(event, result);
//        logger.debug("parse mail finish {}", event.getId());
//        String key = String.format(Constant.mail163ResultKey, event.getUserid());
//        redis.set(key, JSON.toJSONString(spiderResult), 300); // expire in 5 minutes
//        key = String.format(Constant.MAIL163TASKQUEUE, event.getAccount());
//        redis.del(key);
    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: loginUp
     * @date 2015年10月27日 下午5:50:15
     * @author yukf
     */
    private void loginUp(Mail163SpiderEvent event) {
        String s = String.format(loginUrl, event.getAccount());
        HttpPost httpPost = new HttpPost(s);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(createFormEntity(event), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode body error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event, null));
    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: reloginUp
     * @date 2015年10月29日 下午3:06:34
     * @author yukf
     */
    private void reloginUp(Mail163SpiderEvent event) {
        logger.debug("do relogin up {}", event.getId());
        String s = String.format(reloginUrl, event.getAccount());
        HttpPost httpPost = new HttpPost(s);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(createReloginFormEntity(event), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode body error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        setHeader(s, httpPost, event);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event, null));
    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: getidUp
     * @date 2015年10月29日 下午12:15:58
     * @author yukf
     */
    private void getidUp(Mail163SpiderEvent event) {
        logger.debug("do get id up {}", event.getId());
        HttpGet httpGet = new HttpGet(getidUrl);
        setHeader(getidUrl, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));
    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: getImgUp
     * @date 2015年10月29日 下午12:24:22
     * @author yukf
     */
    private void getImgUp(Mail163SpiderEvent event) {
        logger.debug("do get img up {}", event.getId());
        String s = String.format(getImgUrl, event.getVerifyid());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));

    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: vfyUp
     * @date 2015年10月29日 下午2:40:03
     * @author yukf
     */
    private void vfyUp(Mail163SpiderEvent event) {
        logger.debug("do vfy up {}", event.getId());
        String s = String.format(vfyUrl, event.getvCode(), event.getVerifyid());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));

    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: nextUp
     * @date 2015年10月29日 下午3:32:45
     * @author yukf
     */
    private void nextUp(Mail163SpiderEvent event) {
        logger.debug("do next up {}", event.getId());
        HttpGet httpGet = new HttpGet(event.getNext());
        setHeader(event.getNext(), httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));
    }

    /**
     * @param event
     */
    private void entryUp(Mail163SpiderEvent event) {
        logger.debug("do entry up {}", event.getId());
        HttpGet httpGet = new HttpGet(event.getEntryUrl());
        setHeader(event.getEntryUrl(), httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));

    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: mailListUp
     * @date 2015年10月27日 下午5:50:19
     * @author yukf
     */
    private void mailListUp(Mail163SpiderEvent event) {
        logger.debug("do get mail list up {}", event.getId());
        String s = String.format(mailListUrl, event.getSid());
        HttpPost httpPost = new HttpPost(s);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(createMailUpFormEntity(event), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode body error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        setHeader(s, httpPost, event);
        httpPost.setHeader("Accept", "text/javascript"); // 为了接收json格式的列表
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event, null));
    }

    private void taskQueue(Mail163SpiderEvent event) {
        String key = String.format(Constant.MAIL163TASKQUEUE, event.getAccount());
        String value = redis.getStringByKey(key);
        if (StringUtils.isNotBlank(value)) {
            SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
            event.getDeferredResult().setResult(exception.toString());
            return;
        }
        redis.set(key, INQUEUE, 600);
        event.setState(Mail163SpiderState.MAIL);
        super.taskQueue(event);
    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: mailUp
     * @date 2015年10月27日 下午6:28:33
     * @author yukf
     */
    public void mailUp(Mail163SpiderEvent event) {
        logger.debug("do get mail up {}", event.getId());
        Envelope envelope = event.getEnvelopes().poll();
        String mailid = envelope.getId();
        String s = String.format(mailUrl, mailid);
        HttpGet httpGet = new HttpGet(s);
        httpGet.setHeader("Host", "mail.163.com");
        httpGet.setHeader("Referer", "http://mail.163.com/js6/main.jsp?sid=" + event.getSid() + "&df=mail163_letter");
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, envelope));
    }

    private void parseTaskQueue(Mail163SpiderEvent event) {
        event.setState(Mail163SpiderState.PARSE);
        super.parseTaskQueue(event);
    }

    private List<? extends NameValuePair> createFormEntity(Mail163SpiderEvent event) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("url2", "http://mail.163.com/errorpage/error163.htm"));
        params.add(new BasicNameValuePair("savelogin", "0"));
        params.add(new BasicNameValuePair("username", event.getAccount()));
        params.add(new BasicNameValuePair("password", event.getPassword()));
        return params;
    }

    private List<? extends NameValuePair> createReloginFormEntity(Mail163SpiderEvent event) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", event.getAccount()));
        params.add(new BasicNameValuePair("password", event.getPassword()));
        return params;
    }

    /**
     * @param event
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: createMailUpFormEntity
     * @date 2015年10月27日 下午6:00:29
     * @author yukf
     */
    private List<? extends NameValuePair> createMailUpFormEntity(Mail163SpiderEvent event) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("var",
                "<?xml version=\"1.0\"?><object><int name=\"fid\">1</int><boolean name=\"skipLockedFolders\">false</boolean><string name=\"order\">date</string><boolean name=\"desc\">true</boolean><int name=\"start\">0</int><int name=\"limit\">300</int><boolean name=\"topFirst\">true</boolean><boolean name=\"returnTotal\">true</boolean><boolean name=\"returnTag\">true</boolean></object>"));
        return params;
    }

    /**
     * create spider result
     *
     * @date 2015年10月30日 上午11:56:10
     * @author yukf
     */
    private SpiderResult createSpiderResult(Mail163SpiderEvent event) {
        SpiderResult spiderResult = new SpiderResult();
        spiderResult.setAccount(event.getAccount());
        spiderResult.setPassword(event.getPassword());
        return spiderResult;
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private Mail163SpiderEvent event;
        private Envelope envelope;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(Mail163SpiderEvent event, Envelope envelope) {
            this.event = event;
            this.envelope = envelope;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                // extract cookie
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case LOGIN:
                        loginDown(result);
                        break;
                    case RELOGIN:
                        reloginDown(result);
                        break;
                    case GETID:
                        getidDown(result);
                        break;
                    case GETIMG:
                        getImgDown(result);
                        break;
                    case VFY:
                        vfyDown(result);
                        break;
                    case NEXT:
                        nextDown(result);
                        break;
                    case ENTRY:
                        entryDown(result);
                        break;
                    case MAILLIST:
                        mailListDown(result);
                        break;
                    case MAIL:
                        mailDown(result, envelope);
                        break;
                    case ERROR:
                        errorHandle(event);
                        break;
                }
            } catch (Exception e) {
                logger.error("unexpected error {}", event.getId(), e);
                event.setException(e);
            }
            // next step
            if (skipNextStep)
                return;
            onEvent(event);
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: loginDown
         * @date 2015年10月27日 下午4:40:08
         * @author yukf
         */
        private void loginDown(HttpResponse result) {
            logger.debug("login down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                if (resultStr.contains("<title>relogin</title>")) {
                    // 需要验证码
                    event.setState(Mail163SpiderState.RELOGIN);
                } else {
                    Matcher matcher = loginDownPattern.matcher(resultStr);
                    if (matcher.find()) {
                        // 用户名密码正确
                        String group = matcher.group();
                        String sid = group.split("=")[1];
                        sid = sid.substring(0, sid.length() - 3);
                        event.setSid(sid);
                        event.setState(Mail163SpiderState.MAILLIST);
                    } else {
                        logger.debug("password incorrect {} password: {}", event.getId(), event.getPassword());
                        event.setException(new SpiderException(1013, "登陆失败"));
                        return;
                    }
                }

            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: reloginDown
         * @date 2015年10月29日 下午3:14:51
         * @author yukf
         */
        private void reloginDown(HttpResponse result) {
            logger.debug("relogin down {}", event.getId());
            event.setState(Mail163SpiderState.GETID);

        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: getidDown
         * @date 2015年10月29日 下午12:19:38
         * @author yukf
         */
        private void getidDown(HttpResponse result) {
            logger.debug("get id down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                event.setVerifyid(resultStr);
                event.setState(Mail163SpiderState.GETIMG);
            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }

        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: getImgDown
         * @date 2015年10月29日 下午12:27:05
         * @author yukf
         */
        private void getImgDown(HttpResponse result) {
            logger.debug("get img down {}", event.getId());
            try {
                HttpEntity httpEntity = result.getEntity();
                InputStream imgStream = httpEntity.getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int rd = -1;
                byte[] bytes = new byte[1024];
                while ((rd = imgStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, rd);
                }
                byte[] rbyte = baos.toByteArray();
                String imgStr = Base64.getEncoder().encodeToString(rbyte);
                // save to file, used for debug
                if (appConfig.inDevMode()) {
                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
                    FileOutputStream fs = new FileOutputStream(appConfig.getUploadPath() + "/" + event.getUserid() + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }
                // store event to redis
                event.setState(Mail163SpiderState.VFY); // next step is to verify
                String key = String.format(Constant.mail163ImportKey, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); // expire in 5
                logger.debug("need vfy code {}", event.getId());
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
                return;

            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }

        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: vfyDown
         * @date 2015年10月29日 下午2:43:04
         * @author yukf
         */
        private void vfyDown(HttpResponse result) {
            logger.debug("vfy vcode down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                if (resultStr.contains("200") && resultStr.contains("loginCookie")) {
                    //验证码通过
                    int stIndex = resultStr.indexOf("http");
                    String location = resultStr.substring(stIndex).trim();
                    event.setNext(location);
                    event.setState(Mail163SpiderState.NEXT);
                } else {
                    // 验证码错误，返回到GETID
                    logger.debug("vfy code incorrect {}", event.getId());
                    event.setState(Mail163SpiderState.GETID);
                }
            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: nextDown
         * @date 2015年10月29日 下午3:34:53
         * @author yukf
         */
        private void nextDown(HttpResponse result) {
            logger.debug("next down {}", event.getId());
            try {
                Header header = result.getFirstHeader("Location");
                String location = header.getValue();
                event.setEntryUrl(location.trim());
                event.setState(Mail163SpiderState.ENTRY);
            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: entryDown
         * @date 2015年10月29日 下午4:25:13
         * @author yukf
         */
        private void entryDown(HttpResponse result) {
            logger.debug("entry down {}", event.getId());
            try {
                Header header = result.getFirstHeader("Location");
                String location = header.getValue().trim();
                String sid = location.split("=")[1];
                sid = sid.substring(0, sid.length() - 3);
                event.setSid(sid);
                event.setState(Mail163SpiderState.MAILLIST);
            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }
        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: mailListDown
         * @date 2015年10月27日 下午6:03:35
         * @author yukf
         */
        private void mailListDown(HttpResponse result) {
            logger.debug("mail list down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                List<Envelope> envelopes = solveDateAndToEnvelope(resultStr);
                int tsize = envelopes.size();
                int totalMail = Math.min(tsize, MAX_MAIL_SIZE);
                ConcurrentLinkedDeque<Envelope> hasEnve = event.getEnvelopes();
                for (int i = 0; i < totalMail; i++) {
                    hasEnve.addLast(envelopes.get(i));
                }
                String strCookie = String.format(cookieStr, event.getSid());
                List<HttpCookieEx> cookies = HttpCookieEx.parse(strCookie);
                event.getCookieList().addAll(cookies);

                hasEnve = ParserReactor.getInstance().envelopesFilter(hasEnve, event.getDate());
                event.setEnvelopes(hasEnve);

                if (hasEnve.size() > 0) {
                    logger.info("get mail list done, need to download " + hasEnve.size() + " mails {}", event.getId());
                    event.setTotalMail(hasEnve.size());
                    event.setState(Mail163SpiderState.TASKQUEUE);
                } else {
                    // 完成，返回结果
                    SpiderResult spiderResult = createSpiderResult(event);
                    String key = String.format(Constant.mail163ResultKey, event.getUserid());
                    Map<String, BillResult> billResultMap = new HashMap<>();
                    redis.set(key, JSON.toJSONString(billResultMap), 300);
                    //redis.set(key, JSON.toJSONString(spiderResult), 300); // expire 5 minutes
                    event.getDeferredResult().setResult(new SpiderException(1019, "开始获取邮件").toString());
                    logger.info("get mail list done, do not need to download mails {}", event.getId());
                    skipNextStep = true;
                }
            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }

        }

        /**
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: mailDown
         * @date 2015年10月28日 上午9:50:29
         * @author yukf
         */
        private void mailDown(HttpResponse result, Envelope envelope) {
            logger.debug("mail down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                int done = event.getMailDone().incrementAndGet();
                MailSrc mailSrc = new MailSrc();
                String from = envelope.getFrom();
                if (from.contains("<") && from.contains(">")) {
                    from = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
                }
                mailSrc.setDate(new Date(Long.parseLong(envelope.getSentDate())));
                mailSrc.setBody(resultStr);
                mailSrc.setTitle(envelope.getSubject());
                mailSrc.setFrom(from);
                event.getMailSrcs().add(mailSrc);

                if (appConfig.inDevMode()) {
                    File parent = new File(appConfig.getUploadPath() + "/mail/");
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    MailQQSpider.writeToFile(appConfig.getUploadPath() + "/mail/", mailSrc.getTitle() + Math.random() + ".html", mailSrc.getBody());
                }
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(Mail163SpiderState.PARSETASKQUEUE);
                } else {
                    skipNextStep = true;
                }
            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }
        }

        @Override
        public void failed(Exception ex) {
            logger.debug("request {} failed: {}", event.getId(), ex.getMessage());
            if (event.getState() == Mail163SpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(Mail163SpiderState.PARSETASKQUEUE);
                    onEvent(event);
                }
            }
        }

        @Override
        public void cancelled() {
            logger.debug("connection closed");
        }
    }

}
