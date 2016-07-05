package com.caitu99.lsp.spider.mail139;

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
import com.caitu99.lsp.model.spider.mail139.Mail139SpiderEvent;
import com.caitu99.lsp.model.spider.mail139.Mail139SpiderState;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Pattern;

/**
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: sdfsdf
 * @date 2015年10月26日 下午4:28:45
 * @Copyright (c) 2015-2020 by caitu99
 */
public class Mail139Spider extends AbstractMailSpider {
    private static final Logger logger = LoggerFactory.getLogger(Mail139Spider.class);

    private static final String isSmsLoginCookieStr = "_139_index_isSmsLogin=0; Path=/";
    private static final String mailListStr = "<object><int name=\"fid\">1</int><string name=\"order\">receiveDate</string><string name=\"desc\">1</string><int name=\"start\">%s</int><int name=\"total\">%s</int><string name=\"topFlag\">top</string><int name=\"sessionEnable\">2</int></object>";
    private static final String loginPageUrl = "http://mail.10086.cn/";
    private static final String loginUrl = "https://mail.10086.cn/Login/Login.ashx?_fv=4&cguid=1731593717306&_=43f8b97c4c97fcb18b12d8749e4757dfa1bef9b8&resource=indexLogin";
    private static final String mailListUrl = "http://appmail.mail.10086.cn/s?func=mbox:listMessages&sid=%s&&comefrom=54&cguid=1521530276899";
    private static final String reloginUrl = "https://reg.163.com/login.jsp?username=%s&url=http://entry.mail.163.com/coremail/fcg/ntesdoor2";
    private static final String mailUrl = "http://appmail.mail.10086.cn/RmWeb/view.do?func=view:readMessage&comefrom=54&sid=%s&cguid=0.10826920131475021&mid=%s&callback=readMailReady&fid=1";
    private static final String getidUrl = "https://reg.163.com/services/getid";
    private static final String getImgUrl = "http://imagecode.mail.10086.cn/getimage?clientid=1&r=%s";
    private static final String vfyUrl = "http://mail.10086.cn/s?func=login:picLogin";

    private static final Pattern loginDownPattern = Pattern.compile("(?<=top.location.href = \").*(?=;</)");
    private static final int MAX_MAIL_SIZE = 200;

    public Mail139Spider(MailBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    @Override
    public void onEvent(MailSpiderEvent event) {
        Mail139SpiderEvent l39Event = (Mail139SpiderEvent) event;
        try {
            switch (l39Event.getState()) {
                case LOGINPAGE:
                    loginPageUp(l39Event);
                    break;
                case LOGIN:
                    loginUp(l39Event);
                    break;
                case GETIMG:
                    getImg(l39Event);
                    break;
                case VFY:
                    vfyCodeUp(l39Event);
                    break;
                case MAILLIST:
                    mailListUp(l39Event);
                    break;
                case TASKQUEUE:
                    taskQueue(l39Event);
                    break;
                case MAIL:
                    mailUp(l39Event);
                    break;
                case PARSETASKQUEUE:
                    parseTaskQueue(l39Event);
                    break;
                case PARSE:
                    parseMail(l39Event);
                    break;
                case ERROR:
                    errorHandle(l39Event);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", event.getId(), e);
            l39Event.setException(e);
            errorHandle(l39Event);
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
    private void parseMail(Mail139SpiderEvent event) {

        logger.debug("do parse mail {}", event.getId());
        try {
            ParserContext context = new ParserContext();
            context.setUserId(Integer.parseInt(event.getUserid()));
            String account = event.getAccount();
            if (!account.contains("@")) {
                account = account + "@139.com";
            }
            context.setAccount(account);
            context.setMailSrcs(event.getMailSrcs());
            context.setRedisKey(Constant.mail139ResultKey);
            ParserReactor.getInstance().process(context);
        }
        finally {
            String key = String.format(Constant.MAIL139TASKQUEUE, event.getAccount());
            redis.del(key);
        }
//        logger.debug("do parse mail {}", event.getId());
//        MailParser parser = MailParser.getInstance();
//        Map<String, List<UserCardVo>> result = parser.execute(event.getMailSrcs());
//        SpiderResult spiderResult = createSpiderResult(event, result);
//        logger.debug("parse mail finish {}", event.getId());
//        String key = String.format(Constant.mail139ResultKey, event.getUserid());
//        redis.set(key, JSON.toJSONString(spiderResult), 300); // expire in 5
//        // minutes
//        key = String.format(Constant.MAIL139TASKQUEUE, event.getAccount());
//        redis.del(key);
    }

    /**
     * @date 2015年11月10日 上午11:29:18
     * @author yukf
     */
    private void loginPageUp(Mail139SpiderEvent event) {
        logger.debug("do get login page up {}", event.getId());
        HttpGet httpGet = new HttpGet(loginPageUrl);
        setHeader(loginPageUrl, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));
    }

    /**
     * @date 2015年11月10日 下午2:20:46
     * @author yukf
     */
    private void loginUp(Mail139SpiderEvent event) {
        logger.debug("do login up {}", event.getId());
        HttpPost httpPost = new HttpPost(loginUrl);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(createLoginFormEntity(event), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode body error {}", event.getId(), e);
            event.setException(e);
            return;
        }

        // 加入cookie
        List<HttpCookieEx> hasCookies = event.getCookieList();
        boolean exist = false;
        for (HttpCookieEx cookieEx : hasCookies) {
            String value = cookieEx.getValue();
            String[] arr = value.split("=");
            if (arr != null && arr[0].equals("_139_index_isSmsLogin")) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            List<HttpCookieEx> cookies = HttpCookieEx.parse(isSmsLoginCookieStr);
            hasCookies.addAll(cookies);
        }

        setHeader(loginUrl, httpPost, event);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event, null));
    }

    /**
     * 2015年11月16日 yukf
     */
    private void getImg(Mail139SpiderEvent event) {
        logger.debug("do get img up {}", event.getId());
        Random ran = new Random(new Date().getTime());
        if (StringUtils.isEmpty(event.getClient())) {
            event.setClient("" + ran.nextInt());
        }
        String s = String.format(getImgUrl, ran.nextInt(10));
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));
    }

    private void vfyCodeUp(Mail139SpiderEvent event) {
        logger.debug("do vfy code up {}", event.getId());
        HttpPost httpPost = new HttpPost(vfyUrl);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(createVfyCodeFormEntity(event), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode vfy code body error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        setHeader(vfyUrl, httpPost, event);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event, null));
    }


    /**
     * @date 2015年11月10日 下午2:20:46
     * @author yukf
     */
    private void mailListUp(Mail139SpiderEvent event) {
        logger.debug("do get mail list up {}", event.getId());
        String entityStr = String.format(mailListStr, 1, 100);
        String s = String.format(mailListUrl, event.getSid());
        HttpPost httpPost = new HttpPost(s);
        try {
            httpPost.setEntity(new StringEntity(entityStr));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode body error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        setHeader(s, httpPost, event);
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event, null));
    }

    /**
     * @date 2015年11月10日 下午3:15:30
     * @author yukf
     */
    private void taskQueue(Mail139SpiderEvent event) {
        String key = String.format(Constant.MAIL139TASKQUEUE, event.getAccount());
        String value = redis.getStringByKey(key);
        if (StringUtils.isNotBlank(value)) {
            SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
            event.getDeferredResult().setResult(exception.toString());
            return;
        }
        redis.set(key, INQUEUE, 600);
        event.setState(Mail139SpiderState.MAIL);
        super.taskQueue(event);
    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: reloginUp
     * @date 2015年10月29日 下午3:06:34
     * @author yukf
     */
    private void reloginUp(Mail139SpiderEvent event) {
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
    private void getidUp(Mail139SpiderEvent event) {
        logger.debug("do get id up {}", event.getId());
        HttpGet httpGet = new HttpGet(getidUrl);
        setHeader(getidUrl, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, null));
    }

    /**
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: mailUp
     * @date 2015年10月27日 下午6:28:33
     * @author yukf
     */
    public void mailUp(Mail139SpiderEvent event) {
        logger.debug("do get mail up {}", event.getId());
        Envelope envelope = event.getEnvelopes().poll();
        String mailid = envelope.getId();
        String s = String.format(mailUrl, event.getSid(), mailid);
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event, envelope));
    }

    private void parseTaskQueue(Mail139SpiderEvent event) {
        event.setState(Mail139SpiderState.PARSE);
        super.parseTaskQueue(event);
    }

    private List<? extends NameValuePair> createLoginFormEntity(Mail139SpiderEvent event) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("UserName", event.getAccount()));
        params.add(new BasicNameValuePair("Password", event.getPassword()));
        params.add(new BasicNameValuePair("VerifyCode", ""));
        return params;
    }

    private List<? extends NameValuePair> createReloginFormEntity(Mail139SpiderEvent event) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", event.getAccount()));
        params.add(new BasicNameValuePair("password", event.getPassword()));
        return params;
    }

    private List<? extends NameValuePair> createVfyCodeFormEntity(Mail139SpiderEvent event) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("VerifyCode", event.getvCode()));
        params.add(new BasicNameValuePair("u", event.getAccount()));
        params.add(new BasicNameValuePair("s", "0"));
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
    private List<? extends NameValuePair> createMailUpFormEntity(Mail139SpiderEvent event) {

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
    private SpiderResult createSpiderResult(Mail139SpiderEvent event) {
        SpiderResult spiderResult = new SpiderResult();
        spiderResult.setAccount(event.getAccount());
        spiderResult.setPassword(event.getPassword());
        return spiderResult;
    }

    /**
     * @date 2015年11月11日 下午12:16:31
     * @author yukf
     */
    private List<Envelope> parseJsonToEnvelope(String resultStr) {
        List<Envelope> envelopes = new ArrayList<>();
        JSONObject jsonObj = JSONObject.parseObject(resultStr);
        JSONArray jsonArr = jsonObj.getJSONArray("var");
        if (jsonArr != null) {
            for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject jsonEvelope = jsonArr.getJSONObject(i);
                envelopes.add(getEnvelopeFromJsonObj(jsonEvelope));
            }
        }
        return envelopes;
    }

    /**
     * @date 2015年11月11日 下午12:19:41
     * @author yukf
     */
    private Envelope getEnvelopeFromJsonObj(JSONObject jsonObj) {
        Envelope envelope = new Envelope();
        envelope.setId(jsonObj.getString("mid"));
        envelope.setFrom(jsonObj.getString("from"));
        envelope.setSubject(jsonObj.getString("subject"));
        envelope.setSentDate(jsonObj.getString("sendDate"));
        envelope.setReceiveDate(jsonObj.getString("receiveDate"));
        envelope.setTo(jsonObj.getString("to"));
        return envelope;
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private Mail139SpiderEvent event;
        private Envelope envelope;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(Mail139SpiderEvent event, Envelope envelope) {
            this.event = event;
            this.envelope = envelope;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                // extract cookie
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case LOGINPAGE:
                        loginPageDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case GETIMG:
                        getImgDown(result);
                        break;
                    case VFY:
                        vfyCodeDown(result);
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
         * @date 2015年11月10日 上午11:34:07
         * @author yukf
         */
        private void loginPageDown(HttpResponse result) {
            logger.debug("login page down {}", event.getId());
            event.setState(Mail139SpiderState.LOGIN);
        }

        /**
         * @date 2015年11月10日 下午2:25:14
         * @author yukf
         */
        private void loginDown(HttpResponse result) {
            logger.debug("login down {}", event.getId());
            try {
                Header[] headers = result.getHeaders("Set-Cookie");
                boolean loginSuccess = false;
                if (headers != null) {
                    for (Header tHeader : headers) {
                        String value = tHeader.getValue();
                        String[] arr = value.split("=");
                        if (arr != null && arr[0].equals("Os_SSo_Sid")) {
                            int stIndex = value.indexOf("=");
                            int endIndex = value.indexOf(";");
                            event.setSid(value.substring(stIndex + 1, endIndex));
                            loginSuccess = true;
                            break;
                        }
                    }
                }
                if (loginSuccess) {
                    event.setState(Mail139SpiderState.MAILLIST);
                } else {
                    // 判断是否出了验证码
                    Header header = result.getFirstHeader("Location");
                    if (header != null) {
                        String location = header.getValue();
                        if (location.indexOf("imgverify") != -1) {
                            event.setState(Mail139SpiderState.GETIMG);
                            return;
                        }
                    }
                    logger.debug("password incorrect {}", event.getId());
                    event.setException(new SpiderException(1013, "登录失败"));
                    return;

                }
            } catch (Exception e) {
                logger.error("get result exception {}", event.getId(), e);
                event.setException(e);
            }
        }

        private void getImgDown(HttpResponse result) {
            logger.debug("get mail down {}", event.getId());
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
                    // FileOutputStream fs = new
                    // FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
                    FileOutputStream fs = new FileOutputStream(
                            appConfig.getUploadPath() + "/" + event.getUserid() + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }

                // store event to redis
                event.setState(Mail139SpiderState.VFY); // next step is to
                // verify
                // vCode
                String key = String.format(Constant.mail139ImportKey, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); // expire in 5
                // minutes
                logger.debug("need vfy code {}", event.getId());
                // return vcode to user
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
                return;
            } catch (Exception e) {
                logger.error("get img result exception {}", event.getId(), e);
                event.setException(e);
            }
        }

        private void vfyCodeDown(HttpResponse result) {
            logger.debug("get vfy code down {}", event.getId());
            Header[] headers = result.getHeaders("Set-Cookie");
            boolean loginSuccess = false;
            if (headers != null) {
                for (Header tHeader : headers) {
                    String value = tHeader.getValue();
                    String[] arr = value.split("=");
                    if (arr != null && arr[0].equals("Os_SSo_Sid")) {
                        int stIndex = value.indexOf("=");
                        int endIndex = value.indexOf(";");
                        event.setSid(value.substring(stIndex + 1, endIndex));
                        loginSuccess = true;
                        break;
                    }
                }
            }
            if (loginSuccess) {
                event.setState(Mail139SpiderState.MAILLIST);
            } else {
                event.setState(Mail139SpiderState.GETIMG);
            }
        }

        /**
         * @date 2015年11月11日 上午11:05:19
         * @author yukf
         */
        private void mailListDown(HttpResponse result) {
            logger.debug("mail list down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                List<Envelope> envelopes = parseJsonToEnvelope(resultStr);
                ConcurrentLinkedDeque<Envelope> hasEnve = event.getEnvelopes();
                int totalMail = Math.min(envelopes.size(), MAX_MAIL_SIZE);
                for (int i = 0; i < totalMail; i++) {
                    hasEnve.addLast(envelopes.get(i));
                }
                hasEnve = ParserReactor.getInstance().envelopesFilter(hasEnve, event.getDate());

                event.setEnvelopes(hasEnve);
                if (hasEnve.size() > 0) {
                    logger.info("get mail list done, need to download " + hasEnve.size() + " mails {}", event.getId());
                    event.setTotalMail(hasEnve.size());
                    event.setState(Mail139SpiderState.TASKQUEUE);
                } else {
                    // 完成，返回结果
                    SpiderResult spiderResult = createSpiderResult(event);
                    String key = String.format(Constant.mail139ResultKey, event.getUserid());
                    Map<String, BillResult> billResultMap = new HashMap<>();
                    redis.set(key, JSON.toJSONString(billResultMap), 300);
                    //redis.set(key, JSON.toJSONString(spiderResult), 300); // expire
                    // in
                    // 5
                    // minutes
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
                    MailQQSpider.writeToFile(appConfig.getUploadPath() + "/mail/",
                            mailSrc.getTitle() + Math.random() + ".html", mailSrc.getBody());
                }
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(Mail139SpiderState.PARSETASKQUEUE);
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
            if (event.getState() == Mail139SpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(Mail139SpiderState.PARSETASKQUEUE);
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
