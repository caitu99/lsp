package com.caitu99.lsp.spider.mailsina;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.parser.BillResult;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.model.spider.SpiderResult;
import com.caitu99.lsp.model.spider.mailsina.*;
import com.caitu99.lsp.parser.ParserReactor;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailBodySpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.ScriptHelper;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.cookie.CookieHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lion on 2015/11/10 0010.
 */
public class MailSinaSpider extends AbstractMailSpider {

    //请求头
    protected static final String HTTPHEAD_REFERER = "Referer";
    private final static Integer MAXMAIL = 200;     //最大获取邮件数
    private final static Logger logger = LoggerFactory.getLogger(MailSinaSpider.class);
    private static final String REFERERURL = "http://mail.sina.com.cn/";
    private static final String REFERERURL_MAIL = "http://mail.sina.com.cn/?from=mail";
    private static final String REFERERURL_FOR_GETMAIL = "http://m0.mail.sina.com.cn/classic/index.php";
    private static final String PRE_LOGIN_GET_URL = "http://login.sina.com.cn/sso/prelogin.php?entry=cnmail&callback=sinaSSOController.preloginCallBack" +
            "&su=%s" +
            "&rsakt=mod&client=ssologin.js(v1.4.18)" +
            "&_=%s";
    private static final String LOGIN_STEP1_POST_URL = "http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.18)&_=%s";
    private static final String LOGIN_STEP2_4_GET_URL_SUFFIX = "&callback=sinaSSOController.doCrossDomainCallBack&scriptId=ssoscript0&client=ssologin.js(v1.4.18)&_=%s";
    private static final String GET_MAIL_LIST_URL = "http://m0.mail.sina.com.cn/wa.php?a=list_mail";
    private static final String CGI_SLA_URL = "http://mail.sina.com.cn/cgi-bin/sla.php?a=%s&b=%s&c=%s";
    private static final String CLASSIC_INDEX_URL = "http://m0.mail.sina.com.cn";
    private static final String MAIL_URL = "http://m0.mail.sina.com.cn/classic/readmail.php?webmail=1&fid=new&mid=%s&ts=16751'";
    private static final String ACTIVE_URL = "http://login.sina.com.cn/cgi/pin.php?r=%s&s=%s&p=%s";
    //正则
    private static Pattern checkPreloginDownPattern = Pattern.compile("(?<=\\().*(?=\\))");
    private static Pattern login_step2_downPattern = Pattern.compile("(?<=\\().*(?=\\))");
    private static Pattern login_step3_downPattern = Pattern.compile("(?<=\\().*(?=\\))");
    private static Pattern login_step4_downPattern = Pattern.compile("(?<=\\().*(?=\\))");
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

    public MailSinaSpider(MailBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    public static void writeToFile(String filePath, String filename, String html) {
        File fileDir = new File(filePath);
        if (fileDir.isDirectory()) {
            fileDir.mkdirs();
        }
        filename = filename.replace(":", " ");
        File file = new File(fileDir, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(html.getBytes("gbk"));
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEvent(MailSpiderEvent event) {
        MailSinaSpiderEvent sinaEvent = (MailSinaSpiderEvent) event;
        try {
            switch (sinaEvent.getState()) {
                case PRE_LOGIN:
                    pre_login_up(sinaEvent);
                    break;
                case LOGIN_STEP1:
                    login_step1_up(sinaEvent);
                    break;
                case GETVERIFY:
                    getVerifyCodeUp(sinaEvent);
                    break;
                case LOGIN_SETP2:
                    login_step2_up(sinaEvent);
                    break;
                case LOGIN_SETP3:
                    login_step3_up(sinaEvent);
                    break;
                case LOGIN_SETP4:
                    login_step4_up(sinaEvent);
                    break;
                case CGI_SLA:
                    cgi_sla_up(sinaEvent);
                    break;
                case SENDSID:
                    send_sid_up(sinaEvent);
                    break;
                case CLASSICINDEX:
                    classic_index_up(sinaEvent);
                    break;
                case GETMAIL_LIST:
                    getMailListUP(sinaEvent);
                    break;
                case TASKQUEUE:
                    taskQueue(sinaEvent);
                    break;
                case MAIL:
                    getMailUp(sinaEvent);
                    break;

                case PARSETASKQUEUE:
                    parseTaskQueue(sinaEvent);
                    break;
                case PARSE:
                    parseSinaMail(sinaEvent);
                    break;
                case ERROR:
                    errorHandle(sinaEvent);
                    break;

                default:
                    ;
            }
        } catch (Exception e) {
            logger.error("request up error {},{}", sinaEvent.getId(), e);
            sinaEvent.setException(e);
            errorHandle(event);
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

    private void pre_login_up(MailSinaSpiderEvent event) {
        logger.debug("do pre login for {}", event.getId());
        try {
            String url = String.format(PRE_LOGIN_GET_URL,
                    ScriptHelper.encryptSinaUsername(event.getAccount()),
                    System.currentTimeMillis());
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }

    }

    private void login_step1_up(MailSinaSpiderEvent event) {
        logger.debug("do login step1 for {}", event.getId());
        try {
            String url = String.format(LOGIN_STEP1_POST_URL, System.currentTimeMillis());
            HttpPost httpPost = new HttpPost(url);
            setHeader(url, httpPost, event);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("entry", "freemail"));
            nvps.add(new BasicNameValuePair("gateway", "1"));
            nvps.add(new BasicNameValuePair("from", ""));
            nvps.add(new BasicNameValuePair("savestate", "30"));
            nvps.add(new BasicNameValuePair("useticket", "0"));
            nvps.add(new BasicNameValuePair("pagerefer", ""));
            nvps.add(new BasicNameValuePair("su", ScriptHelper.encryptSinaUsername(event.getAccount())));
            nvps.add(new BasicNameValuePair("service", "sso"));
            nvps.add(new BasicNameValuePair("servertime", event.getServerTime()));
            nvps.add(new BasicNameValuePair("nonce", event.getNonce()));
            nvps.add(new BasicNameValuePair("pwencode", "rsa2"));
            nvps.add(new BasicNameValuePair("rsakv", event.getRsakv()));
            nvps.add(new BasicNameValuePair("sp", ScriptHelper.encryptSinaPassword(event.getPassword(),
                    event.getPubkey(), Long.valueOf(event.getServerTime()), event.getNonce())));
            nvps.add(new BasicNameValuePair("sr", "1920*1080"));
            nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
            nvps.add(new BasicNameValuePair("cdult", "3"));
            nvps.add(new BasicNameValuePair("domain", "sina.com.cn"));
            nvps.add(new BasicNameValuePair("returntype", "TEXT"));
            if (event.isVerisy()) {
                nvps.add(new BasicNameValuePair("prelt", "103"));
                nvps.add(new BasicNameValuePair("pcid", event.getPcid()));
                nvps.add(new BasicNameValuePair("door", event.getvCode()));
                event.setVerisy(false);
            } else {
                nvps.add(new BasicNameValuePair("prelt", "102"));
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            httpPost.setHeader(HTTPHEAD_REFERER, REFERERURL_MAIL);
            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void getVerifyCodeUp(MailSinaSpiderEvent event) {
        logger.debug("get verify code  for {}", event.getId());
        try {
            String url = String.format(ACTIVE_URL, Math.floor(Math.random() * 100000000), 0, event.getPcid());
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("get verify code unexpected on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void login_step2_up(MailSinaSpiderEvent event) {
        logger.debug("do login step2 for {}", event.getId());
        try {
            String url = event.getUrl_step2() + String.format(LOGIN_STEP2_4_GET_URL_SUFFIX, System.currentTimeMillis());
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL_MAIL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("login step2 unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void login_step3_up(MailSinaSpiderEvent event) {
        logger.debug("do login step3 for {}", event.getId());
        try {
            String url = event.getUrl_step3() + String.format(LOGIN_STEP2_4_GET_URL_SUFFIX, System.currentTimeMillis());
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL_MAIL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("login step3 unexpected error  on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void login_step4_up(MailSinaSpiderEvent event) {
        logger.debug("do login step4 for {}", event.getId());
        try {
            String url = event.getUrl_step4() + String.format(LOGIN_STEP2_4_GET_URL_SUFFIX, System.currentTimeMillis());
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL_MAIL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("login step4 unexpected error  on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void cgi_sla_up(MailSinaSpiderEvent event) {
        logger.debug("cgi_sla_up for {}", event.getId());
        try {
            String url = String.format(CGI_SLA_URL, System.currentTimeMillis(), System.currentTimeMillis() + 400, 0);
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL_MAIL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("login step4 unexpected error  on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void send_sid_up(MailSinaSpiderEvent event) {
        logger.debug("send_sid_up for {}", event.getId());
        try {
            String url = event.getLocation();
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL_MAIL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("login step4 unexpected error  on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void classic_index_up(MailSinaSpiderEvent event) {
        logger.debug("send_sid_up for {}", event.getId());
        try {
            String url = CLASSIC_INDEX_URL + event.getLocation();
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, REFERERURL_MAIL);
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("login step4 unexpected error  on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void getMailListUP(MailSinaSpiderEvent event) {
        logger.debug("do get mail list for {}", event.getId());
        try {
            String url = GET_MAIL_LIST_URL;
            HttpPost httpPost = new HttpPost(url);
            setHeader(url, httpPost, event);
            httpPost.setHeader(HTTPHEAD_REFERER, REFERERURL_FOR_GETMAIL);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("webmail", "1"));
            nvps.add(new BasicNameValuePair("tag", "-1"));
            nvps.add(new BasicNameValuePair("pageno", String.valueOf(event.getCurPage().get())));    //首页为1
            nvps.add(new BasicNameValuePair("type", "0"));
            nvps.add(new BasicNameValuePair("sorttype", "desc"));
            nvps.add(new BasicNameValuePair("order", "htime"));
            nvps.add(new BasicNameValuePair("fid", "new"));
//            nvps.add(new BasicNameValuePair("c", System.currentTimeMillis()+""));
//            nvps.add(new BasicNameValuePair("d", (System.currentTimeMillis()+50)+""));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("login step4 unexpected error  on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void taskQueue(MailSinaSpiderEvent event) {
        String key = String.format(Constant.MAILSINATASKQUEUE, event.getAccount());
        String value = redis.getStringByKey(key);
        if (StringUtils.isNotBlank(value)) {
            SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
            event.getDeferredResult().setResult(exception.toString());
            return;
        }
        redis.set(key, INQUEUE, 600);
        event.setState(MailSinaSpiderState.MAIL);
        super.taskQueue(event);
    }

    private void getMailUp(MailSinaSpiderEvent event) {
        logger.debug("do get sina mail up {}", event.getId());

        if (event.getMailDone().get() == 0 && event.getTicTac().get("spider_mail") == null) {
            event.getTicTac().tic("spider_mail");
        }

        Envelope envelope = event.getEnvelopes().poll();
        String s = String.format(MAIL_URL, envelope.getId());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    private void parseTaskQueue(MailSinaSpiderEvent event) {
        event.setState(MailSinaSpiderState.PARSE);
        super.parseTaskQueue(event);
    }

    private void parseSinaMail(MailSinaSpiderEvent event) {
        logger.debug("do parse sina mail {}", event.getId());
        try {
            ParserContext context = new ParserContext();
            context.setUserId(Integer.parseInt(event.getUserid()));
            context.setAccount(event.getAccount());
            context.setMailSrcs(event.getMailSrcs());
            context.setRedisKey(Constant.MAILSINARESULTKEY);
            ParserReactor.getInstance().process(context);
        }
        finally {
            String key = String.format(Constant.MAILSINATASKQUEUE, event.getAccount());
            redis.del(key);
        }
    }

    private SpiderResult createSpiderResult(MailSinaSpiderEvent event) {
        SpiderResult spiderResult = new SpiderResult();
        spiderResult.setAccount(event.getAccount());
        spiderResult.setPassword(event.getPassword());
        return spiderResult;
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {
        private MailSinaSpiderEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(MailSinaSpiderEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case PRE_LOGIN:
                        pre_login_down(result);
                        break;
                    case LOGIN_STEP1:
                        login_step1_down(result);
                        break;
                    case GETVERIFY:
                        getVerifyCodeDown(result);
                        break;
                    case LOGIN_SETP2:
                        login_step2_down(result);
                        break;
                    case LOGIN_SETP3:
                        login_step3_down(result);
                        break;
                    case LOGIN_SETP4:
                        login_step4_down(result);
                        break;
                    case CGI_SLA:
                        cgi_sla_down(result);
                        break;
                    case SENDSID:
                        send_sid_down(result);
                        break;
                    case CLASSICINDEX:
                        classic_index_down(result);
                        break;
                    case GETMAIL_LIST:
                        getMailListDown(result);
                        break;
                    case MAIL:
                        getMailDown(result);
                        break;
                    case ERROR:
                        errorHandle(event);
                        break;
                }

            } catch (Exception e) {
                logger.error("unexpected error on {}, {}", event.getId(), e);
                event.setException(e);
            }
            if (skipNextStep) {
                return;
            }
            onEvent(event);
        }

        @Override
        public void failed(Exception ex) {
            logger.debug("request {} failed: {}", event.getId(), ex.getMessage());
            if (event.getState() == MailSinaSpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(MailSinaSpiderState.PARSETASKQUEUE);
                }
            }
        }

        @Override
        public void cancelled() {
            logger.debug("request cancelled: {}", event.getId());
            if (event.getState() == MailSinaSpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(MailSinaSpiderState.PARSETASKQUEUE);
                    onEvent(event);
                }
            }
        }

        private void pre_login_down(HttpResponse result) {
            logger.debug("pre login down {}", event.getId());
            try {
                String reslutStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = checkPreloginDownPattern.matcher(reslutStr);

                if (matcher.find()) {
                    String group = matcher.group(0);

                    Map<String, String> map =
                            JSON.parseObject(group, new TypeReference<Map<String, String>>() {
                            });
                    String retCode = map.get(ConsForPrelogin.RETCODE);

                    switch (retCode) {
                        case "0":
                            logger.debug("get pre login message for: {}", event.getId());
                            event.setServerTime(map.get(ConsForPrelogin.SERVERTIME));
                            event.setPcid(map.get(ConsForPrelogin.PCID));
                            event.setNonce(map.get(ConsForPrelogin.NONCE));
                            event.setRsakv(map.get(ConsForPrelogin.RSAKV));
                            event.setPubkey(map.get(ConsForPrelogin.PUBKEY));
                            event.setIs_openlock(map.get(ConsForPrelogin.IS_OPENLOCK));
                            event.setExectime(map.get(ConsForPrelogin.EXECTIME));
                            event.setState(MailSinaSpiderState.LOGIN_STEP1);
                            break;
                        default:
                            logger.error("error pre login response {} reslutStr: {}", event.getId(), reslutStr);
                            event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                    }
                } else {
                    logger.error("error pre login response {} reslutStr: {}", event.getId(), reslutStr);
                    event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void login_step1_down(HttpResponse result) {
            logger.debug("login step1 down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Map<String, String> map =
                        JSON.parseObject(resultStr, new TypeReference<Map<String, String>>() {
                        });
                String retCode = map.get(ConsForLoginStep1.RETCODE);
                switch (retCode) {
                    case "0":
                        logger.debug("get three urls for next step for {}", event.getId());
                        String url = map.get(ConsForLoginStep1.CROSSDOMAINRULLIST);
                        url = url.replace("\"", "");
                        url = url.replace("[", "");
                        url = url.replace("]", "");
                        String[] urls = url.split(",");
                        event.setUrl_step2(urls[0]);
                        event.setUrl_step3(urls[1]);
                        event.setUrl_step4(urls[2]);

                        event.setState(MailSinaSpiderState.LOGIN_SETP2);
                        break;
                    case "101": //密码错误
                        logger.debug("username or password incorrect {}", event.getId());
                        String message = map.get(ConsForLoginStep1.REASON);
                        event.setException(new SpiderException(1013, message));
                        return;
                    case "4038":
                        logger.debug("Try to log in too often, please log in again later");
                        String message4038 = map.get(ConsForLoginStep1.REASON);
                        event.setException(new SpiderException(1013, message4038));
                        return;
                    case "2070":    //验证码错误 需要重新获取验证码

                    case "4049":    //需要验证码
                        logger.debug("need verify code");
                        String message4049 = map.get(ConsForLoginStep1.REASON);
                        event.setState(MailSinaSpiderState.GETVERIFY);
                        break;

                    default:
                        logger.error("unknown error when login sina mail {}, resultStr: {}", event.getId(), resultStr);
                        event.setException(new SpiderException(1013, "登录新浪邮箱的时候发生未知错误"));
                        return;

                }

            } catch (Exception e) {
                logger.error("get result exception on login step1", e);
            }
        }

        private void getVerifyCodeDown(HttpResponse result) {
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
                if (appConfig.inDevMode()) {
                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
                    FileOutputStream fs = new FileOutputStream(appConfig.getUploadPath() + "/" + UUID.randomUUID() + "_" + new Date().getTime() + event.getUserid() + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }
                event.setState(MailSinaSpiderState.LOGIN_STEP1); //
                event.setVerisy(true);
                // vCode
                String key = String.format(Constant.MAILSINAIMPORTKEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); // expire in 5
                // minutes
                logger.debug("need verify code {}", event.getId());
                // return vcode to user
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
                return;

            } catch (Exception e) {
                logger.error("get sina mail verify code exception", e);
                event.setException(e);
            }
        }

        private void login_step2_down(HttpResponse result) {
            logger.debug("login step2 down {}", event.getId());

            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                //对字符串进行处理
                resultStr = resultStr.replace("\\u", "");
                Matcher matcher = login_step2_downPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group(0);
                    Map<String, String> map =
                            JSON.parseObject(group, new TypeReference<Map<String, String>>() {
                            });
                    String retresult = map.get(ConsForLoginStep2.RESULT);
                    if ("true".equals(retresult)) {
                        event.setState(MailSinaSpiderState.LOGIN_SETP3);
                    } else {
                        logger.error("error pre login response {} reslutStr: {}", event.getId(), resultStr);
                        event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                    }
                } else {
                    logger.error("error pre login response {} reslutStr: {}", event.getId(), resultStr);
                    event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                }

            } catch (Exception e) {
                logger.error("get result exception in login_step2_down {}", e.getMessage());
                event.setException(e);
            }
        }

        private void login_step3_down(HttpResponse result) {
            logger.debug("login step3 down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = login_step3_downPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group(0);
                    Map<String, String> map =
                            JSON.parseObject(group, new TypeReference<Map<String, String>>() {
                            });
                    String retCode = map.get(ConsForLoginStep3.RETCODE);
                    if ("0".equals(retCode)) {
                        event.setState(MailSinaSpiderState.LOGIN_SETP4);
                    } else {
                        logger.error("error login step3 response {} reslutStr: {}", event.getId(), resultStr);
                        event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                    }
                } else {
                    logger.error("error pre login response {} reslutStr: {}", event.getId(), resultStr);
                    event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void login_step4_down(HttpResponse result) {
            logger.debug("login step4 down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = login_step4_downPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group(0);
                    Map<String, String> map =
                            JSON.parseObject(group, new TypeReference<Map<String, String>>() {
                            });
                    String retCode = map.get(ConsForLoginStep4.RETCODE);
                    if ("20000000".equals(retCode)) {
                        event.setState(MailSinaSpiderState.CGI_SLA);
                    } else {
                        logger.error("error login step4 response {} reslutStr: {}", event.getId(), resultStr);
                        event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                    }
                } else {
                    logger.error("error login step4 response {} reslutStr: {}", event.getId(), resultStr);
                    event.setException(new SpiderException(1028, " 新浪邮箱预登录返回数据格式未知"));
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void cgi_sla_down(HttpResponse result) {
            logger.debug("login step4 down {}", event.getId());
            try {
                if (HttpServletResponse.SC_MOVED_TEMPORARILY == result.getStatusLine().getStatusCode()) {
                    event.setLocation(result.getFirstHeader("Location").getValue());
                    if (ACTIVE_URL.equals(event.getLocation())) {
                        logger.error("this account needs activing by phone");
                        event.setException(new SpiderException(1030, "该邮箱登录时需要手机验证激活"));
                        return;
                    }
                    event.setState(MailSinaSpiderState.SENDSID);
                } else {
                    logger.error("error login step4 down response {} code {}", event.getId(), result.getStatusLine().getStatusCode());
                }

            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void send_sid_down(HttpResponse result) {
            logger.debug("send sid down {}", event.getId());
            try {
                if (HttpServletResponse.SC_MOVED_TEMPORARILY == result.getStatusLine().getStatusCode()) {
                    event.setLocation(result.getFirstHeader("Location").getValue());
                    event.setState(MailSinaSpiderState.CLASSICINDEX);
                } else {
                    logger.error("error send sid down response {} code {}", event.getId(), result.getStatusLine().getStatusCode());
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void classic_index_down(HttpResponse result) {
            logger.debug("classic index down {}", event.getId());
            try {
                if (HttpServletResponse.SC_OK == result.getStatusLine().getStatusCode()) {
                    event.setState(MailSinaSpiderState.GETMAIL_LIST);
                } else {
                    logger.error("error classic index down response {} code {}", event.getId(), result.getStatusLine().getStatusCode());
                    event.setException(new SpiderException(2000, "error classic index downresponse"));
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private List<Envelope> parseMailToEnvelope(SinaMailList sinaMailList) {
            List<Envelope> envelopes = new ArrayList<>();
            int num = Math.min(sinaMailList.getData().getMailList().size(), Integer.valueOf(sinaMailList.getData().getPagesize()));
            ArrayList<String[]> mailList = sinaMailList.getData().getMailList();
            for (int i = 0; i < num; i++) {
                String[] strs = mailList.get(i);
                Envelope envelope = new Envelope();
                envelope.setId(strs[0]);
                envelope.setFrom(strs[1]);
                envelope.setTo(strs[2]);
                envelope.setSubject(strs[3]);
                envelope.setSentDate(strs[4]);
                envelope.setReceiveDate(strs[4]);

                envelopes.add(envelope);
            }

            return envelopes;
        }

        private void getMailListDown(HttpResponse result) {
            logger.debug("get mail list down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                SinaMailList sinaMailList = JSON.parseObject(resultStr, SinaMailList.class);
                if ("true".equals(sinaMailList.getResult())) {
                    Integer curpage = Integer.valueOf(sinaMailList.getData().getCurrentpage());
                    Integer pagenum = Integer.valueOf(sinaMailList.getData().getPagenum());
                    event.getCurPage().incrementAndGet();
//                    if (sinaMailList.getData().getMailList().size() > 0) {
                    List<Envelope> envelopes = parseMailToEnvelope(sinaMailList);
                    event.getEnvelopes().addAll(envelopes);
                    if ((curpage < pagenum) && (event.getEnvelopes().size() < MAXMAIL)) {    //还要继续获取邮件列表
                        event.setState(MailSinaSpiderState.GETMAIL_LIST);
                        return;//继续下一次获取邮件列表
                    }
//                    }
                    ConcurrentLinkedDeque<Envelope> hasEnve = event.getEnvelopes();
                    hasEnve = ParserReactor.getInstance().envelopesFilter(hasEnve, event.getDate());
                    event.setEnvelopes(hasEnve);
                    if (hasEnve.size() > 0) {
                        logger.info("get mail list done, " + hasEnve.size() + " mails need parser {}", event.getId());
                        event.setTotalMail(hasEnve.size());
                        event.setState(MailSinaSpiderState.TASKQUEUE);
                    } else {
                        // 没有邮件需要解析，返回结果
                        SpiderResult spiderResult = createSpiderResult(event);
                        String key = String.format(Constant.MAILSINARESULTKEY, event.getUserid());
                        
                        Map<String, BillResult> billResultMap = new HashMap<>();
                        redis.set(key, JSON.toJSONString(billResultMap), 300);
                        
                        //redis.set(key, JSON.toJSONString(spiderResult), 300); // expire in 5 minutes

//                        event.getDeferredResult().setResult(new SpiderException(1029, "没有邮件需要解析").toString());
                        logger.info("get mail list done, do not need to download mails {}", event.getId());
                        event.getDeferredResult().setResult(
                                new SpiderException(1019, "开始获取邮件").toString());
                        skipNextStep = true;
                    }
                } else {
                    logger.error("get mail list down: response code is not true");
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void getMailDown(HttpResponse result) {
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                int done = event.getMailDone().incrementAndGet();
                try {
                    SinaMailSrc sinaMailSrc = JSON.parseObject(resultStr, SinaMailSrc.class);
                    MailSrc mailSrc = parseMailToMailSrc(sinaMailSrc);
                    event.getMailSrcs().add(mailSrc);
                    if (appConfig.inDevMode()) {
                        File parent = new File(appConfig.getUploadPath() + "/mail/");
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        writeToFile(appConfig.getUploadPath() + "/mail/", mailSrc.getTitle() + UUID.randomUUID() + ".html", mailSrc.getBody());
                    }

                } catch (Exception e) {
                    logger.error("error parse mail to mailsrc {} resultStr: {}", event.getId());
                }
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getDate());
                    event.setState(MailSinaSpiderState.PARSETASKQUEUE);
                } else {
                    skipNextStep = true;
                }
            } catch (Exception e) {
                logger.error("get result exception when get sina mail ", e);
                event.setException(e);
            } finally {
                if (event.getMailDone().get() == event.getTotalMail()) {
                    event.getTicTac().tac("spider_mail");
                    logger.info("spider sina mail spend {}", event.getTicTac().elapsed("spider_mail"));
                }
            }

        }

        private MailSrc parseMailToMailSrc(SinaMailSrc sinaMailSrc) {
            MailSrc mailSrc = new MailSrc();
            SinaMailContent content = sinaMailSrc.getData();
            mailSrc.setTitle(content.getSubject());
            mailSrc.setBody(content.getBody());
            mailSrc.setDate(new Date(Long.valueOf(content.getDate()) * 1000));
            mailSrc.setFrom(content.getFrom());
            return mailSrc;
        }

        private SpiderResult createSpiderResult(MailSinaSpiderEvent event) {
            SpiderResult spiderResult = new SpiderResult();
            spiderResult.setAccount(event.getAccount());
            spiderResult.setPassword(event.getPassword());
            return spiderResult;
        }
    }
}

class ConsForPrelogin {
    public static final String RETCODE = "retcode";
    public static final String SERVERTIME = "servertime";
    public static final String PCID = "pcid";
    public static final String NONCE = "nonce";
    public static final String PUBKEY = "pubkey";
    public static final String RSAKV = "rsakv";
    public static final String IS_OPENLOCK = "is_openlock";
    public static final String EXECTIME = "exectime";

}

class ConsForLoginStep1 {
    public static final String RETCODE = "retcode";
    public static final String CROSSDOMAINRULLIST = "crossDomainUrlList";
    public static final String REASON = "reason";
}

class ConsForLoginStep2 {
    public static final String RESULT = "result";
}

class ConsForLoginStep3 {
    public static final String RETCODE = "retcode";
}


class ConsForLoginStep4 {
    public static final String RETCODE = "retcode";
}