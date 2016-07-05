package com.caitu99.lsp.spider.hotmail;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.parser.BillResult;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.model.spider.SpiderResult;
import com.caitu99.lsp.model.spider.hotmail.HotmailItem;
import com.caitu99.lsp.model.spider.hotmail.MailHotmailSpiderEvent;
import com.caitu99.lsp.model.spider.hotmail.MailHotmailSpiderState;
import com.caitu99.lsp.parser.ParserReactor;
import com.caitu99.lsp.spider.AbstractMailSpider;
import com.caitu99.lsp.spider.MailBodySpider;
import com.caitu99.lsp.spider.MailParserTask;
import com.caitu99.lsp.utils.HtmlHelper2;
import com.caitu99.lsp.utils.MailDownloaderUtils;
import com.caitu99.lsp.utils.SpringContext;
import com.caitu99.lsp.utils.XStringUtil;
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

import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lion on 2015/11/14 0014.
 */
public class MailHotmailSpider extends AbstractMailSpider {
    public static final String STEP1_URL_prefix = "https://login.live.com/login.srf?wa=wsignin1.0&rpsnv=12&ct=";
    protected static final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
    //请求头
    protected static final String HTTPHEAD_REFERER = "Referer";
    private final static Logger logger = LoggerFactory.getLogger(MailHotmailSpider.class);
    private static final String DEFAULT_URL = "https://mail.live.com/default.aspx";
    private static final String STEP1_URL_suffix = "&rver=6.4.6456.0&wp=MBI_SSL_SHARED&wreply=https:%2F%2Fsnt151.mail.live.com%2Fdefault.aspx%3Frru%3Dinbox&lc=2052&id=64855&mkt=zh-cn&cbcxt=mai";
    private static final String NEXT_PAGE = "https://snt151.mail.live.com/ol/mail.fpp?cnmn=Microsoft.Msn.Hotmail.Ui.Fpp.MailBox.GetInboxData&ptid=0&a=%s&au=%s";
    private static final String GETMAIL_URL = "https://snt151.mail.live.com/ol/mail.fpp?cnmn=Microsoft.Msn.Hotmail.Ui.Fpp.MailBox.GetInboxData&ptid=0&a=%s&au=%s";
    //正则
    private static Pattern getloginurlMatcher = Pattern.compile("urlPost:\'[^']+\'");
    private static Pattern getPPFT = Pattern.compile("<input type=\"hidden\" name=\"PPFT\"[^']+\'");
    private static Pattern passport = Pattern.compile(",P:\'[^\']*\'");
    //    private static Pattern replace = Pattern.compile("(?<=\")https://[^\"]+rru=inbox(?=\")");
    private static Pattern replace = Pattern.compile("(?<=window\\.location\\.replace\\(\").*?(?=\")");
    private static Pattern sessionId = Pattern.compile("\"SessionId\":\"[^\"]+\"");
    private static Pattern hotmailuserid = Pattern.compile("\"AuthUser\":\"[^\"]+\"");
    //private static Pattern findmaillist = Pattern.compile("\"cm[^\"]{23}\":\\s*new\\s*HM\\.RollupData\\(new\\s*HM\\.Rollup\\([^)]+\\)[^)]+\\)[^)]+\\)}");
    private static Pattern findMailMsgCollection = Pattern.compile("(?<=\\{)\"cm.{23}[^}]*\"\\)(?=\\})");
    //private static Pattern findmaillist = Pattern.compile("(?<=\\{)\"cm[0-9a-zA-Z]{23}[^}]*\"\\)(?=\\})");
    private static Pattern findmaillist = Pattern.compile("\"cm.{23}\":.*?[\\w.]*@\\w*\\.\\w*\"\\)");
    private static Pattern mailNum = Pattern.compile("(?<=PageNavigationMsgRange\">)\\d+(?=\\s*封邮件</div>)");
    private static Integer cnt = 0;
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

    public MailHotmailSpider(MailBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    public static HotmailItem str2hotMailItem(String string) {
        HotmailItem hotmailItem = new HotmailItem();
        //mailcode
        Pattern pattern = Pattern.compile("^\"[^\"]+\"");
        Matcher matcher_mailCode = pattern.matcher(string);
        if (matcher_mailCode.find()) {
            String group = matcher_mailCode.group(0);
            hotmailItem.setMailCode(group.substring(1, group.length() - 1));
        }

//        pattern = Pattern.compile("\"1447\\d{9}\"\\)[^)]+\\d{19}[^)]+\".*@.*\"", Pattern.DOTALL);
        pattern = Pattern.compile("\"\\d{13}\"\\)[^)]+\\d{19}[^)]+\".*@(\\w*)\\.\\w*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            String group = matcher.group(0);
            pattern = Pattern.compile("\"\\d{13}\"");
            matcher = pattern.matcher(group);
            if (matcher.find()) {
                String timemis = matcher.group(0);
                hotmailItem.setTimeLong(timemis.substring(1, timemis.length() - 1));
            }
            pattern = Pattern.compile("\"\\d{19}\"");
            matcher = pattern.matcher(group);
            if (matcher.find()) {
                String mailid = matcher.group(0);
                hotmailItem.setMailId(mailid.substring(1, mailid.length() - 1));
            }

            pattern = Pattern.compile(",\\d+,\"\\d{19}\"");
            matcher = pattern.matcher(group);
            if (matcher.find()) {
                String sendCode = matcher.group(0);
                pattern = Pattern.compile(",\\d+,");
                matcher = pattern.matcher(sendCode);
                if (matcher.find()) {
                    sendCode = matcher.group(0);
                    hotmailItem.setSendCode(sendCode.substring(1, sendCode.length() - 1));
                }
            }

            pattern = Pattern.compile("([\\w.]*)@(\\w*)\\.\\w*");
            matcher = pattern.matcher(group);
            if (matcher.find()) {
                String senderMail = matcher.group(0);
                hotmailItem.setSender(senderMail);
            }
            hotmailItem.setOtherCode("45");
        }
        //title
        pattern = Pattern.compile("(?<=],null,\")([^\"]*)(?=\")");
        matcher = pattern.matcher(string);
        if (matcher.find()) {
            String group = matcher.group(0);
            hotmailItem.setTitle(group);
        }

        //time
        pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
        matcher = pattern.matcher(string);
        if (matcher.find()) {
            String group = matcher.group(0);
            hotmailItem.setTimeStr(group.replace(":", "\\:"));
        }

        //size
        //pattern = Pattern.compile("(?<=\")(\\d+)(?=\\sKB\")");
        //matcher = pattern.matcher(string);
        //if (matcher.find()) {
        //    String group = matcher.group(0);
        //    hotmailItem.setSize(group);
        //}
        //else {
            hotmailItem.setSize("123");
        //}



        return hotmailItem;
    }

    public static void getCookies(List<HttpCookieEx> cookieList, List<HttpCookieEx> list) {
        //for Set-Cookie

        for (HttpCookieEx cookieEx : list) {
            if (!cookieEx.hasExpired() && !cookieList.contains(cookieEx)) {
                cookieList.add(cookieEx);
            }
        }
    }

    public static void writeToFile(String filePath, String filename, String html) {
        File fileDir = new File(filePath);
        if (fileDir.isDirectory()) {
            fileDir.mkdirs();
        }
        filename = filename.replaceAll(":", " ").replaceAll("\\|", "");
        File file = new File(fileDir, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(html.getBytes("utf-8"));
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

    public static ConcurrentLinkedDeque<HotmailItem> getBankEnvelope(ConcurrentLinkedDeque<HotmailItem> hotmailItems, long searchTime) {
        ConcurrentLinkedDeque<HotmailItem> bankEnvelopes = new ConcurrentLinkedDeque<HotmailItem>();
        for (HotmailItem hotmailItem : hotmailItems) {
            long mailDate = Long.parseLong(hotmailItem.getTimeLong());
            String subject = hotmailItem.getTitle();
            subject = XStringUtil.deleteSpace(subject);
            if (subject.indexOf("账单") >= 0 || subject.indexOf("账户概要") >= 0 || subject.indexOf("积分") >= 0 || subject.indexOf("賬戶概要") >= 0) {
                String bankConfigStr = MailDownloaderUtils.getBankInstanceEmail(hotmailItem.getTitle(), hotmailItem.getSender());
                if (StringUtils.isNotEmpty(bankConfigStr)) {
                    if ("0".equals(bankConfigStr.split(";")[1])) {
                        if (mailDate >= searchTime) {
                            bankEnvelopes.add(hotmailItem);
                        }
                    } else {
                        if (mailDate >= XStringUtil.getLastSeasonDay().getTime()) {
                            bankEnvelopes.add(hotmailItem);
                        }
                    }
                }
            }
        }
        return bankEnvelopes;
    }

    @Override
    protected void setHeader(String uriStr, HttpMessage httpGet, MailSpiderEvent event) {
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", userAgent);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    @Override
    public void onEvent(MailSpiderEvent event) {
        MailHotmailSpiderEvent hotmailEvent = (MailHotmailSpiderEvent) event;

        try {
            switch (hotmailEvent.getState()) {
                case DEFAULT:
                    defaultUp(hotmailEvent);
                    break;
                case GETLOGINURL:
                    getLoginUrlUp(hotmailEvent);
                    break;
                case LOGIN:
                    loginUp(hotmailEvent);
                    break;
                case BEGINGETVERIFYCODE:
                    begin_get_verify_up(hotmailEvent);
                    break;
                case GETVERIFYIMG:
                    get_verifyimg_up(hotmailEvent);
                    break;
                case GOTORRU_INBOX:
                    goto_rru_inbox_up(hotmailEvent);
                    break;
                case CHANGEVIEW:
                    changev_view_up(hotmailEvent);
                    break;
                case CHANGEVIEW_NEXT:
                    change_view_up_next(hotmailEvent);
                    break;
                case GETNEXTPAGE:
                    get_nextpage_up(hotmailEvent);
                    break;
                case TASKQUEUE:
                    taskQueue(hotmailEvent);
                    break;
                case GETMAIL:
                    get_mail_up(hotmailEvent);
                    break;
                case PARSETASKQUEUE:
                    parseTaskQueue(hotmailEvent);
                    break;
                case PARSE:
                    parseHotmailMail(hotmailEvent);
                    break;
                case ERROR:
                    errorHandle(hotmailEvent);
                    break;

                default:
                    ;
            }
        } catch (Exception e) {
            logger.error("request up error {},{}", hotmailEvent.getId(), e);
            hotmailEvent.setException(e);
            errorHandle(event);
        }
    }

    private void change_view_up_next(MailHotmailSpiderEvent event) {
        try {
            String url = event.getChangviewurl_next();
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void changev_view_up(MailHotmailSpiderEvent event) {
        try {
            String url = event.getChangviewurl();
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void defaultUp(MailHotmailSpiderEvent event) {

        try {
            cnt = 0;
            String url = DEFAULT_URL;
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void getLoginUrlUp(MailHotmailSpiderEvent event) {
        logger.debug("get login url and login message");
        try {
            String url = event.getLocation();
            event.setRefurlforlogin(url);
            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
//            httpGet.setHeader(, );
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void loginUp(MailHotmailSpiderEvent event) {
        logger.debug("loginUp");
        try {
            String url = event.getLogin_url();
            Long timeStart = System.currentTimeMillis();
            int sleepTimes = (int) (Math.random() * 2000 + 1000);
            Thread.sleep(sleepTimes);
            Long timeEnd = System.currentTimeMillis();//new Date((long) (System.currentTimeMillis()+Math.random()*10000)).getTime();;
            HttpPost httpPost = new HttpPost(url);
            List<HttpCookieEx> listCookies = new ArrayList<>();
            HttpCookieEx cookieEx1 = new HttpCookieEx("CkTst", "G" + timeStart.toString());
            cookieEx1.setPath("/");
            cookieEx1.setSecure(true);
            cookieEx1.setHttpOnly(true);
            HttpCookieEx cookieEx2 = new HttpCookieEx("wlidperf", "FR=L&ST=" + timeEnd.toString());
            cookieEx2.setPath("/");
            cookieEx2.setHttpOnly(true);
            cookieEx1.setSecure(true);
            listCookies.add(cookieEx1);
            listCookies.add(cookieEx2);

            getCookies(event.getCookieList(), listCookies);
            setHeader(url, httpPost, event);

            httpPost.setHeader(HTTPHEAD_REFERER, event.getRefurlforlogin());

            httpPost.setHeader("Host", "login.live.com");
            httpPost.setHeader("Origin", "https://login.live.com");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.81");
            httpPost.setHeader("Cache-Control", "max-age=0");
            httpPost.removeHeaders("User-Agent");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("loginfmt", event.getAccount() + "@hotmail.com"));
            nvps.add(new BasicNameValuePair("passwd", event.getPassword()));
            nvps.add(new BasicNameValuePair("login", event.getAccount() + "@hotmail.com"));
            if (event.isNeedvir()) {
                nvps.add(new BasicNameValuePair("WLSPHIPSolution", "type=visual,Solution=" + event.getvCode() + ",HIPID=" + event.getHid()));
                event.setNeedvir(false);
            }
            nvps.add(new BasicNameValuePair("type", "11"));
            nvps.add(new BasicNameValuePair("PPFT", event.getPPFT()));
            nvps.add(new BasicNameValuePair("PPSX", event.getPassport()));
            nvps.add(new BasicNameValuePair("idsbho", "1"));
            nvps.add(new BasicNameValuePair("sso", "0"));
            nvps.add(new BasicNameValuePair("NewUser", "1"));
            nvps.add(new BasicNameValuePair("LoginOptions", "3"));
            nvps.add(new BasicNameValuePair("i1", "0"));
            nvps.add(new BasicNameValuePair("i2", "1"));
            nvps.add(new BasicNameValuePair("i3", Long.toString((timeEnd - timeStart) - 30)));
            nvps.add(new BasicNameValuePair("i4", "0"));
            nvps.add(new BasicNameValuePair("i7", "0"));
            nvps.add(new BasicNameValuePair("i12", "1"));
            nvps.add(new BasicNameValuePair("i13", "0"));
            nvps.add(new BasicNameValuePair("i14", "53"));
            nvps.add(new BasicNameValuePair("i15", "1085"));
            nvps.add(new BasicNameValuePair("i17", "0"));
            nvps.add(new BasicNameValuePair("i18", "__Login_Strings|1,__Login_Core|1,"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void begin_get_verify_up(MailHotmailSpiderEvent event) {
        logger.debug("begin_get_verify_up");
        try {
            String url = event.getVerifyUlr();

            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
//            httpGet.setHeader(HTTPHEAD_REFERER, event.getRefurlforlogin());
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void get_verifyimg_up(MailHotmailSpiderEvent event) {
        logger.debug("get default rru inbox");
        try {
            String url = event.getVerifyimgurl();

            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
//            httpGet.setHeader(HTTPHEAD_REFERER, event.getRefurlforlogin());
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void goto_rru_inbox_up(MailHotmailSpiderEvent event) {
        logger.debug("get default rru inbox");
        try {
            String url = event.getLogin_next();

            HttpGet httpGet = new HttpGet(url);
            setHeader(url, httpGet, event);
            httpGet.setHeader(HTTPHEAD_REFERER, event.getRefurlforlogin());
            httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void get_nextpage_up(MailHotmailSpiderEvent event) {
        logger.debug("get_nextpage_up");
        try {
            String url = String.format(NEXT_PAGE, URLEncoder.encode(event.getHotmail_sessionId()), event.getHotmail_userid());
            HttpPost httpPost = new HttpPost(url);

            setHeader(url, httpPost, event);

//            httpPost.setHeader(HTTPHEAD_REFERER, event.getRefurlforlogin());

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("cn", "Microsoft.Msn.Hotmail.Ui.Fpp.MailBox"));
            nvps.add(new BasicNameValuePair("mn", "GetInboxData"));
            LinkedHashMap<String, HotmailItem> map = event.getMailMap();
            ArrayList<Map.Entry<String, HotmailItem>> list = new ArrayList<Map.Entry<String, HotmailItem>>(map.entrySet());
            HotmailItem hotmailItem = list.get(list.size() - 1).getValue();
            nvps.add(new BasicNameValuePair("d", "{[],[],[{\"" +
                    hotmailItem.getMailCode() + "\",\"" +
                    hotmailItem.getTimeStr() + "\",2,38,false,2,null,0,\"flinbox\",null,null,null,0,1,4,true,null,28,null,1}],[],[],[]}"));
//            nvps.add(new BasicNameValuePair("d", "{[],[],[],[{2,"
//                    +hotmailItem.getMailCode()
//                    +",\"flinbox\",{-1,0,0,false},{"
//                    +hotmailItem.getSendCode()+",0,\""
//                    +hotmailItem.getSendCode()+"\",\"flinbox\",0,1,0,0,null,[3],45,\""
//                    +hotmailItem.getSender()+"\"},\""
//                    +hotmailItem.getSender()+"\",true,"+(event.getMailMap().size()-1)+",null,0}],[],[]}"));
            nvps.add(new BasicNameValuePair("v", "1"));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void taskQueue(MailHotmailSpiderEvent event) {
        String key = String.format(Constant.MAILHOTMAILTASKQUEUE, event.getAccount());
        String value = redis.getStringByKey(key);
//        if (StringUtils.isNotBlank(value)) {
//            SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
//            event.getDeferredResult().setResult(exception.toString());
//            return;
//        }
        redis.set(key, INQUEUE, 600);
        event.setState(MailHotmailSpiderState.GETMAIL);
        super.taskQueue(event);
    }

    private void get_mail_up(MailHotmailSpiderEvent event) {
        logger.debug("get_mail_up");
        try {
            String url = String.format(GETMAIL_URL, URLEncoder.encode(event.getHotmail_sessionId()), event.getHotmail_userid());
            HttpPost httpPost = new HttpPost(url);

            setHeader(url, httpPost, event);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("cn", "Microsoft.Msn.Hotmail.Ui.Fpp.MailBox"));
//            nvps.add(new BasicNameValuePair("mn", "GetInboxData"));
            nvps.add(new BasicNameValuePair("mn", "MarkMessagesReadState"));


            HotmailItem hotmailItem = event.getHotmailItems().poll();
            if (hotmailItem != null) {
                if (appConfig.inDevMode()) {
                    File parent = new File(appConfig.getUploadPath() + "/mail/");
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try {
                        writeToFile(appConfig.getUploadPath() + "/mail/", UUID.randomUUID() + " " + hotmailItem.getMailCode() + ".txt", hotmailItem.getMailCode());
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                }
                nvps.add(new BasicNameValuePair("d", "true,[\"" +
                        hotmailItem.getMailCode() + "\"],[{" +
                        hotmailItem.getSendCode() + ",0,\"" +
                        hotmailItem.getMailId() + "\",\"flinbox\",0,1,0,0,null,[3],45,\"" +
                        hotmailItem.getSender() + "\"}],[{\"\",\"flinbox\",null}],false,{[],[],[],[{2,\"" +
                        hotmailItem.getMailCode() + "\",\"flinbox\",{-1,0,0,false},{" +
                        hotmailItem.getSendCode() + ",0,\"" +
                        hotmailItem.getMailId() + "\",\"flinbox\",0,1,0,0,null,[3],45,\"" +
                        hotmailItem.getSender() + "\"},\"" +
                        hotmailItem.getSender() + "\",false," + hotmailItem.getSize() + ",null,0}],[],[]},null"));
//            nvps.add(new BasicNameValuePair("d", "{[],[],[],[{2,"
//                    +hotmailItem.getMailCode()
//                    +",\"flinbox\",{-1,0,0,false},{"
//                    +hotmailItem.getSendCode()+",0,\""
//                    +hotmailItem.getMailId()+"\",\"flinbox\",0,1,0,0,null,[3],45,\""
//                    +hotmailItem.getSender()+"\"},\""
//                    +hotmailItem.getSender()+"\",true,"
//                    +hotmailItem.getSize()+",null,0}],[],[]}"));

                nvps.add(new BasicNameValuePair("v", "1"));

                httpPost.setEntity(new UrlEncodedFormEntity(nvps));

                httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));
            }
        } catch (Exception e) {
            logger.error("unexpected error on {}, {}", event.getId(), e);
            event.setException(e);
        }
    }

    private void parseTaskQueue(MailHotmailSpiderEvent event) {
        event.setState(MailHotmailSpiderState.PARSE);
        super.parseTaskQueue(event);
    }

    private void parseHotmailMail(MailHotmailSpiderEvent event) {
        try {
            logger.debug("do parse mail {}", event.getId());
            ParserContext context = new ParserContext();
            context.setUserId(Integer.parseInt(event.getUserid()));
            String account = event.getAccount();
            if (!account.contains("@")) {
                account = account + "@hotmail.com";
            }
            context.setAccount(account);
            context.setMailSrcs(event.getMailSrcs());
            context.setRedisKey(Constant.MAILHOTMAILRESLUTKEY);
            ParserReactor.getInstance().process(context);
        } finally {
            String key = String.format(Constant.MAILHOTMAILTASKQUEUE, event.getAccount());
            redis.del(key);
        }

    }

    public LinkedHashMap<String, HotmailItem> sortMapByValue(LinkedHashMap<String, HotmailItem> oriMap) {
        LinkedHashMap<String, HotmailItem> sortedMap = new LinkedHashMap<String, HotmailItem>();
        if (oriMap != null && !oriMap.isEmpty()) {
            List<Map.Entry<String, HotmailItem>> entryList = new ArrayList<Map.Entry<String, HotmailItem>>(oriMap.entrySet());
            Collections.sort(entryList, new Comparator<Map.Entry<String, HotmailItem>>() {
                public int compare(Map.Entry<String, HotmailItem> entry1,
                                   Map.Entry<String, HotmailItem> entry2) {
                    Long time1 = 0L;
                    Long time2 = 0L;
                    try {
                        time1 = Long.valueOf(entry1.getValue().getTimeLong());
                        time2 = Long.valueOf(entry2.getValue().getTimeLong());
                        if (time1 > time2)
                            return -1;
                        else if (time1 < time2)
                            return 1;
                        else
                            return 0;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Iterator<Map.Entry<String, HotmailItem>> iter = entryList.iterator();
            Map.Entry<String, HotmailItem> tmpEntry = null;
            while (iter.hasNext()) {
                tmpEntry = iter.next();
                sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
            }
        }
        return sortedMap;
    }

    private SpiderResult createSpiderResult(MailHotmailSpiderEvent event) {
        SpiderResult spiderResult = new SpiderResult();
        spiderResult.setAccount(event.getAccount());
        spiderResult.setPassword(event.getPassword());
        return spiderResult;
    }

    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {
        private MailHotmailSpiderEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(MailHotmailSpiderEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {

            try {
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case DEFAULT:
                        defaultDown(result);
                        break;
                    case GETLOGINURL:
                        getLoginUrlDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case BEGINGETVERIFYCODE:
                        begin_get_verify_down(result);
                        break;
                    case GETVERIFYIMG:
                        get_verifyimg_down(result);
                        break;
                    case GOTORRU_INBOX:
                        goto_rru_inbox_or_get_nextpage_down(result);
                        break;
                    case CHANGEVIEW:
                        changev_view_down(result);
                        break;
                    case CHANGEVIEW_NEXT:
                        change_view_down_next(result);
                        break;
                    case GETNEXTPAGE:
                        goto_rru_inbox_or_get_nextpage_down(result);
                        break;
                    case GETMAIL:
                        get_mail_down(result);
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

        private void change_view_down_next(HttpResponse result) {
            try {
                //String resultStr = EntityUtils.toString(result.getEntity());
                //InputStreamReader stream = new InputStreamReader(new ByteArrayInputStream(resultStr.getBytes()));
                //StringBuilder stringBuilder = HtmlHelper2.xHex2Html(stream);
                //stringBuilder = HtmlHelper2.unicodeDecoded(stringBuilder);
                //stringBuilder = HtmlHelper2.htmlDecoded(stringBuilder);
                //System.out.println(stringBuilder);

                event.setState(MailHotmailSpiderState.GOTORRU_INBOX);

            } catch (Exception e) {
                logger.error("get change view result exception", e);
                event.setException(e);
            }
        }

        private void changev_view_down(HttpResponse result) {
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                //InputStreamReader stream = new InputStreamReader(new ByteArrayInputStream(resultStr.getBytes()));
                //StringBuilder stringBuilder = HtmlHelper2.xHex2Html(stream);
                //stringBuilder = HtmlHelper2.unicodeDecoded(stringBuilder);
                //stringBuilder = HtmlHelper2.htmlDecoded(stringBuilder);
                int statusCode = result.getStatusLine().getStatusCode();
                //if(statusCode == 200 || statusCode == 302 || statusCode == 403)
                if (statusCode == 404 || statusCode == 503) {
                    logger.error("statusCode: {}", statusCode);
                    event.setException(new SpiderException(1013, "登录失败，返回的数据未知"));
                    return;
                }

                String url = "https://sb.scorecardresearch.com/p?c1=2&c2=3000001&c3=&c4=&c5=&c6=&c7=";
                if (event.getChangviewurl().contains("snt151")) {
                    url = url + "https%3a%2f%2fsnt151.mail.live.com%2fol%2fMail.mvc&c15=&cv=2.0&cj=1";
                } else {
                    url = url + "https%3a%2f%2fsnt152.mail.live.com%2fol%2fMail.mvc&c15=&cv=2.0&cj=1";
                }
                logger.info("改变视图的第二个url：" + url);
                event.setChangviewurl_next(url);
                event.setState(MailHotmailSpiderState.CHANGEVIEW_NEXT);
            } catch (Exception e) {
                logger.error("get change view result exception", e);
                event.setException(e);
            }
        }

        @Override
        public void failed(Exception ex) {
            logger.debug("request {} failed: {}", event.getId(), ex.getMessage());
            if (event.getState() == MailHotmailSpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(MailHotmailSpiderState.PARSETASKQUEUE);
                }
            }
        }

        @Override
        public void cancelled() {
            logger.debug("request cancelled: {}", event.getId());
            if (event.getState() == MailHotmailSpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(MailHotmailSpiderState.PARSETASKQUEUE);
                }
            }
        }

        private void defaultDown(HttpResponse result) {
            try {
                if (HttpServletResponse.SC_MOVED_TEMPORARILY == result.getStatusLine().getStatusCode()) {
                    event.setLocation(result.getFirstHeader("Location").getValue());
                    if (event.getLocation() == null) {
                        logger.error("get location exception");
                        event.setException(new Exception("get location exception"));
                    }
                    event.setState(MailHotmailSpiderState.GETLOGINURL);
                } else {
                    logger.error("error login step4 down response {} code {}", event.getId(), result.getStatusLine().getStatusCode());
                }

            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }

        }

        private void getLoginUrlDown(HttpResponse result) {
            logger.debug("get login url down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = getloginurlMatcher.matcher(resultStr);
                Matcher matcher2 = getPPFT.matcher(resultStr);
                int flag = 0;
                if (matcher.find()) {
                    String group = matcher.group(0);
                    event.setLogin_url(group.substring(9, group.length() - 1));

                    if (matcher2.find()) {
                        group = matcher2.group(0);

                        Matcher matcher3 = Pattern.compile("value=\"[^\"]+\"").matcher(group);
                        if (matcher3.find()) {
                            group = matcher3.group(0);
                            event.setPPFT(group.substring(7, group.length() - 1));

                            Matcher matcher4 = passport.matcher(resultStr);
                            if (matcher4.find()) {
                                group = matcher4.group(0);
                                event.setPassport(group.substring(4, group.length() - 1));
                                event.setState(MailHotmailSpiderState.LOGIN);
                            } else {
                                flag = 1;
                            }
                        } else {
                            flag = 1;
                        }
                    } else {
                        flag = 1;
                    }
                } else {
                    flag = 1;
                }
                if (1 == flag) {
                    logger.error("error login params  from {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1008, "返回的登录参数未知数据格式"));
                    return;
                }

            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void loginDown(HttpResponse result) {
            logger.debug("get login  down {}", event.getId());

            try {

                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = replace.matcher(resultStr);

                if (matcher.find()) {
                    String group = matcher.group(0);
                    //   String next = group.substring(25, group.length() - 2);
//                    event.setLogin_next(next);
                    event.setLogin_next(group);
                    event.setState(MailHotmailSpiderState.GOTORRU_INBOX);
                    return;
                } else {
                    Pattern pattern1 = Pattern.compile("(?<=')(https://client\\.hip\\.live\\.com/GetHIP/GetHIP/HIP[^']+)(?=')", Pattern.DOTALL);
                    Matcher matcher1 = pattern1.matcher(resultStr);
                    if (matcher1.find()) {
                        String url = matcher1.group(0);
                        event.setVerifyUlr(url);
                        event.setState(MailHotmailSpiderState.BEGINGETVERIFYCODE);
                        return;
                    }

                    Pattern pattern = Pattern.compile("sErrTxt:.*?。", Pattern.DOTALL);
                    Matcher pass = pattern.matcher(resultStr);
                    if (pass.find()) {
                        logger.debug("用户名或密码错误");
                        String msg = pass.group(0);
                        event.setException(new SpiderException(1013, msg.substring(9, msg.length())));
                        return;
                    }

                    logger.error("登录失败，返回的数据未知,{}", resultStr);
                    event.setException(new SpiderException(1013, "登录失败，返回的数据未知"));
                    return;
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void begin_get_verify_down(HttpResponse result) {
            logger.debug("get begin_get_verify_down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Pattern pattern = Pattern.compile("(?<=')(https://SCU\\.client\\.hip\\.live\\.com/GetHIPData[^']+)(?=')", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(resultStr);
                if (matcher.find()) {
                    String url = matcher.group(0);
                    event.setVerifyimgurl(url);
                    pattern = Pattern.compile("(?<=\\?hid=)(.*)(?=&)", Pattern.DOTALL);
                    Matcher matcher1 = pattern.matcher(url);
                    if (matcher1.find()) {
                        String param = matcher1.group(0);
                        event.setHid(param);
                    } else {
                        event.setException(new SpiderException(1008, "返回的登录参数未知数据格式"));
                    }
                    event.setState(MailHotmailSpiderState.GETVERIFYIMG);
                    return;
                } else {
                    event.setException(new SpiderException(1008, "返回的登录参数未知数据格式"));
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void get_verifyimg_down(HttpResponse result) {
            logger.debug("get_verifyimg_down for{}", event.getId());
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
                event.setState(MailHotmailSpiderState.LOGIN); //
                event.setNeedvir(true);
                // vCode
                String key = String.format(Constant.MAILHOTMAILIMPORTKEY, event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); // expire in 5
                // minutes
                logger.debug("need verify code {}", event.getId());
                // return vcode to user
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
                return;
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void goto_rru_inbox_or_get_nextpage_down(HttpResponse result) {
            logger.debug("get goto_rru_inbox_down {}", event.getId());

            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                InputStreamReader stream = new InputStreamReader(new ByteArrayInputStream(resultStr.getBytes()));
                StringBuilder stringBuilder = HtmlHelper2.xHex2Html(stream);
                stringBuilder = HtmlHelper2.unicodeDecoded(stringBuilder);
                stringBuilder = HtmlHelper2.htmlDecoded(stringBuilder);

                if (event.getState() == MailHotmailSpiderState.GOTORRU_INBOX) {
                    Header header = result.getFirstHeader("Location");
                    if (header != null) {
                        event.setLogin_next(header.getValue());
                        event.setState(MailHotmailSpiderState.GOTORRU_INBOX);

                        logger.debug("--------> group {}: {}", cnt++, event.getLogin_next());
                        return;
                    }
                    Matcher matcher1 = sessionId.matcher(stringBuilder);
                    Matcher matcher2 = hotmailuserid.matcher(stringBuilder);
                    Matcher matcher4 = mailNum.matcher(stringBuilder);

                    if (matcher1.find())//sessionid
                    {
                        String group = matcher1.group(0);
                        group = group.replaceAll("%3d", "=");
                        group = group.substring(13, group.length() - 1);
                        event.setHotmail_sessionId(group);
                    } else {
                        event.setException(new SpiderException(1013, "登录失败，返回的数据未知"));
                        return;
                    }
                    //userid
                    if (matcher2.find()) {
                        String group = matcher2.group(0);
                        group = group.substring(12, group.length() - 1);
                        event.setHotmail_userid(group);
                    } else {
                        event.setException(new SpiderException(1013, "登录失败，返回的数据未知"));
                        return;
                    }
                    if (matcher4.find()) //取邮件数
                    {
                        String group_0 = matcher4.group(0);
                        event.getHotmail().setMailTotal(Integer.parseInt(group_0));
                        event.setMaxPage((event.getHotmail().getMailTotal() + 39) / 40);
                        event.setCurPage(new AtomicInteger(0));
                        if (event.getHotmail().getMailTotal() == 0)        //邮件数为0
                        {
                            SpiderResult spiderResult = createSpiderResult(event);
                            String key = String.format(Constant.MAILHOTMAILRESLUTKEY, event.getUserid());
                            redis.set(key, JSON.toJSONString(spiderResult), 300); // expire in 5 minutes
                            logger.info("get mail list done, do not need to download mails {}", event.getId());
                            event.getDeferredResult().setResult(new SpiderException(1019, "开始获取邮件").toString());
                            skipNextStep = true;
                        }
                    } else {
                        if(!event.isHasChangeView()) {
                            logger.info("需要切换视图");
                            event.setHasChangeView(true);
                            String url = event.getLogin_next();
                            if (url.contains("snt151")) {
                                url = "https://snt151.mail.live.com/mail/OptionsWriter.aspx?";
                            } else {
                                url = "https://snt152.mail.live.com/mail/OptionsWriter.aspx?";
                            }
                            String value = null;
                            List<HttpCookieEx> cookieList = event.getCookieList();
                            for (HttpCookieEx cookie : cookieList) {
                                if (cookie.getName().equals("mt")) {
                                    value = cookie.getValue();
                                    break;
                                }
                            }
                            if (value != null) {
                                url = url + "n=" + Math.random() * 10000000000L + "&mt=" + URLEncoder.encode(value) + "&rpl=1";
                                event.setChangviewurl(url);
                                event.setState(MailHotmailSpiderState.CHANGEVIEW);
                                return;
                            }

                            event.setException(new SpiderException(1013, "登录失败，返回的数据未知"));
                            return;
                        }
                        else {
                            logger.error("切换视图后，仍无法解析数据",stringBuilder);
                            event.setException(new SpiderException(1013, "登录失败，返回的数据未知"));
                            return;
                        }
                    }
                }
//                Matcher matcherFindMailCollection = findMailMsgCollection.matcher(stringBuilder);
//                String maillistStr = null;
//                if (matcherFindMailCollection.find()) {
//
//                }
                Matcher matcher3 = findmaillist.matcher(stringBuilder);
                while (matcher3.find()) {
                    String group = matcher3.group(0);
                    HotmailItem hotmailItem = str2hotMailItem(group);
                    if (checkItemIsNotNull(hotmailItem)) {
                        event.getMailMap().put(hotmailItem.getMailCode(), hotmailItem);
                    } else {
                        logger.error("邮件数据组织失败，HotmailItem有部分数据为空：{},原数据：{}", hotmailItem.toString(), group);
                    }
                }

                event.getHotmail().setCurTotal(event.getMailMap().size());//设置当前多少条记录
                event.setMailMap(sortMapByValue(event.getMailMap()));
                event.getCurPage().incrementAndGet();
                if (event.getHotmail().getCurTotal() < event.getHotmail().getMailTotal()
                        && event.getHotmail().getCurTotal() < 200
                        && event.getCurPage().get() < event.getMaxPage()) {
                    //获取下一页
                    event.setState(MailHotmailSpiderState.GETNEXTPAGE);
                } else {
                    ConcurrentLinkedDeque<HotmailItem> hotmailitems = new ConcurrentLinkedDeque<>();
                    ArrayList<HotmailItem> list = new ArrayList<>();
                    Set<Map.Entry<String, HotmailItem>> sets = event.getMailMap().entrySet();
                    for (Map.Entry<String, HotmailItem> entry : sets) {
                        list.add(entry.getValue());
                    }
                    hotmailitems.addAll(list);

                    hotmailitems = ParserReactor.getInstance().envelopesFilterForHotMail(hotmailitems, event.getDate());
                    if (hotmailitems.size() == 0)        //需要解析的邮件数为0
                    {
                        SpiderResult spiderResult = createSpiderResult(event);
                        String key = String.format(Constant.MAILHOTMAILRESLUTKEY, event.getUserid());

                        Map<String, BillResult> billResultMap = new HashMap<>();
                        redis.set(key, JSON.toJSONString(billResultMap), 300);

                        //redis.set(key, JSON.toJSONString(spiderResult), 300); // expire in 5 minutes
                        logger.info("get mail list done, do not need to download mails {}", event.getId());
                        event.getDeferredResult().setResult(new SpiderException(1019, "开始获取邮件").toString());
                        skipNextStep = true;
                    }
                    event.setHotmailItems(hotmailitems);
                    event.setTotalMail(hotmailitems.size());

                    event.setState(MailHotmailSpiderState.TASKQUEUE);
                    //到获取邮件
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        private void get_mail_down(HttpResponse result) {
            logger.debug("get_mail_down for{}", event.getId());

            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                InputStreamReader stream = new InputStreamReader(new ByteArrayInputStream(resultStr.getBytes()));
                StringBuilder stringBuilder = HtmlHelper2.xHex2Html(stream);
                stringBuilder = HtmlHelper2.unicodeDecoded(stringBuilder);
                stringBuilder = HtmlHelper2.htmlDecoded(stringBuilder);
                int done = event.getMailDone().incrementAndGet();
                MailSrc mailSrc = parseMailToMailSrc(stringBuilder);
                if (mailSrc.getBody() != null && mailSrc.getDate() != null && mailSrc.getTitle() != null) {
                    event.getMailSrcs().add(mailSrc);
                    if (appConfig.inDevMode()) {
                        File parent = new File(appConfig.getUploadPath() + "/mail/");
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        writeToFile(appConfig.getUploadPath() + "/mail/", mailSrc.getTitle() + UUID.randomUUID() + ".html", mailSrc.getBody());
                    }
                } else {
                    if (appConfig.inDevMode()) {
                        File parent = new File(appConfig.getUploadPath() + "/mail/");
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        writeToFile(appConfig.getUploadPath() + "/mail/", "bad" + UUID.randomUUID() + ".html", stringBuilder.toString());
                    }
                }

                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getDate());
                    event.setState(MailHotmailSpiderState.PARSETASKQUEUE);
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }


        boolean checkItemIsNotNull(HotmailItem hotmailItem) {
            if (hotmailItem == null ||
                    hotmailItem.getSender() == null ||
                    hotmailItem.getSendCode() == null ||
                    hotmailItem.getTimeLong() == null ||
                    hotmailItem.getTitle() == null ||
                    //hotmailItem.getSize() == null ||
                    hotmailItem.getMailCode() == null ||
                    hotmailItem.getMailId() == null ||
                    hotmailItem.getTimeStr() == null
                    ) {
                return false;
            }
            return true;
        }

        private MailSrc parseMailToMailSrc(StringBuilder sb) {
            String head = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
                    "        \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>\n" +
                    "    <title></title>\n" +
                    "</head>\n" +
                    "<body>";
            String tail = "</body></html>";
            MailSrc mailSrc = new MailSrc();
            String string = sb.toString();
//            Pattern pattern = Pattern.compile("(?<=\")<.*>(?=\")");
//            Matcher matcher = pattern.matcher(string);
//            int x = 0;
//            if (matcher.find()) {
//                String group = matcher.group();
//                x = string.indexOf(group) + group.length();
//            }

            Pattern pattern = Pattern.compile("(?<=\"\\[\\]\",\"\\[\\]\",false,\")<[\\s\\S]*>(?=\")");
            Matcher matcher = pattern.matcher(string);
            if (matcher.find()) {
                mailSrc.setBody(head + matcher.group() + tail);
            } else {
                mailSrc.setBody(head + string + tail);
            }

            //title
            pattern = Pattern.compile("(?<=],null,\")([^\"]*)(?=\")");
            matcher = pattern.matcher(string);
            if (matcher.find()) {
                String group = matcher.group(0);
                mailSrc.setTitle(group);
            }
            //时间
            pattern = Pattern.compile("(?<=,\")(\\d*)(?=\"\\),null)");
            matcher = pattern.matcher(string);
            if (matcher.find()) {
                String timemis = matcher.group(0);
                mailSrc.setDate(new Date(Long.valueOf(timemis)));
            }

            //发件人
            pattern = Pattern.compile("(?<=HM\\.SenderDetails[^@],null,\").*@.*(?=\",)");
            matcher = pattern.matcher(string);
            if (matcher.find()) {
                mailSrc.setFrom(matcher.group());
            }
            return mailSrc;

        }


    }
}
