package com.caitu99.lsp.spider.mailqq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.Constant;
import com.caitu99.lsp.exception.SpiderException;
import com.caitu99.lsp.model.parser.BillResult;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSpiderEvent;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.model.spider.SpiderResult;
import com.caitu99.lsp.model.spider.mailqq.MailQQSpiderEvent;
import com.caitu99.lsp.model.spider.mailqq.MailQQSpiderState;
import com.caitu99.lsp.model.spider.mailqq.MailQQVerifyResult;
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

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailQQSpider extends AbstractMailSpider {

    private final static Logger logger = LoggerFactory
            .getLogger(MailQQSpider.class);
    private static final String checkUrl = "https://ssl.ptlogin2.qq.com/check?pt_tea=1&uin=%s&appid=522005705&ptlang=2052&r=%s";
    private static final String capShowUrl = "https://ssl.captcha.qq.com/cap_union_show?captype=3&uin=%s&cap_cd=%s&lang=2052&aid=522005705&v=%s";
    private static final String capImgUrl = "https://ssl.captcha.qq.com/getimgbysig?aid=522005705&uin=%s&sig=%s";
    private static final String vfyCapUrl = "https://ssl.captcha.qq.com/cap_union_verify?captype=2&uin=%s&ans=%s&sig=%s&aid=522005705";
    private static final String loginUrl = "https://ssl.ptlogin2.qq.com/login?pt_vcode_v1=%s&pt_verifysession_v1=%s&verifycode=%s&u=%s&p=%s&u1=%s&pt_randsalt=%s&ptlang=2052&low_login_enable=1&low_login_hour=720"
            + "&from_ui=1&fp=loginerroralert&device=2&aid=522005705&daid=4&pt_3rd_aid=0&ptredirect=1&h=1&g=1&pt_uistyle=9";

    private static final String redirectUrl = "http%3A%2F%2Fw.mail.qq.com%2Fcgi-bin%2Flogin%3Fvt%3Dpassport%26vm%3Dwsk%26delegate_url%3D%26f%3Dxhtml%26target%3D%26ss%3D1";
    private static final String refererUrl = "https://ui.ptlogin2.qq.com/cgi-bin/login?style=9&appid=522005705&daid=4&"
            + "s_url=http%3A%2F%2Fw.mail.qq.com%2Fcgi-bin%2Flogin%3Fvt%3Dpassport%26vm%3Dwsk%26delegate_url%3D%26f%3Dxhtml%26target%3D&hln_css=http%3A%2F%2Fmail.qq.com%2Fzh_CN%2Fhtmledition%2Fimages%2Flogo%2Fqqmail%2Fqqmail_logo_default_200h.png&low_login=1&hln_autologin=%E8%AE%B0%E4%BD%8F%E7%99%BB%E5%BD%95%E7%8A%B6%E6%80%81&pt_no_onekey=1";

    private static final String sidUrl = "http://w.mail.qq.com/cgi-bin/login?vt=passport&vm=wsk&delegate_url=&f=xhtml&target=&ss=1";
    private static final String aloneLoginUrl = "https://w.mail.qq.com/cgi-bin/login?sid=";
    private static final String cgiMailListUrl = "http://w.mail.qq.com/cgi-bin/mail_list?ef=js&r=%s&sid=%s&t=mobile_data.json&s=list&page=%s&pagesize=%s&folderid=1&device=ios&app=phone&ver=app";
    private static final String mailUrl = "http://w.mail.qq.com/cgi-bin/readmail?ef=js&r=%s&sid=%s&t=mobile_data.json&s=read&showreplyhead=1&disptype=html&mailid=%s";
    private static final String newSigUrl = "https://ssl.captcha.qq.com/getQueSig?aid=522005705&uin=%s&captype=2&sig=%s*&%s";
    private static final int MAX_MAIL_SIZE = 200;
    private static final int ENVELOPE_PER_PAGE = 100;
    private static final int MAIL_ID_LEN = "ZL3020-lEmqeK23j4cCcGg9hDnnf56"
            .length();
    private static Pattern cap_sigPattern = Pattern
            .compile("(?<=g_click_cap_sig=\")([^\"]*)(?=\";)");
    private static Pattern checkDownPattern = Pattern
            .compile("(?<=\\().*(?=\\))");
    private static Pattern vfyDownPattern = Pattern.compile("(\\{[^\\}]*\\})");
    private static Pattern loginDownPattern = Pattern
            .compile("(?<=\\().*(?=\\))");
    private static Pattern alonePageDownPattern = Pattern
            .compile("(?<=value=\")\\d+(?=\" name=\"ts\")");
    private static Pattern sidDownPattern = Pattern
            .compile("(?<=url=).*(?=\"/><meta http-equiv=\"pragma\")");
    private static Pattern getsidPattern = Pattern
            .compile("(?<=sid=).*(?=&first)");
    private static Pattern newSigDownPattern = Pattern
            .compile("(?<=cap_getCapBySig\\(\").*(?=\")");
    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);

    public MailQQSpider(MailBodySpider mailBodySpider, MailParserTask mailParser) {
        super(mailBodySpider, mailParser);
    }

    /**
     * write string to file(use when test)
     *
     * @param filePath
     * @param filename
     * @param html
     * @Description: (方法职责详细描述, 可空)
     * @Title: writeToFile
     * @date 2015年10月24日 上午9:45:19
     * @author yukf
     */
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

    /**
     * event handle
     *
     * @param event event
     */
    @Override
    public void onEvent(MailSpiderEvent event) {
        MailQQSpiderEvent qqEvent = (MailQQSpiderEvent) event;
        try {
            switch (qqEvent.getState()) {
                case CHECK:
                    checkUp(qqEvent);
                    break;
                case SHOW:
                    showUp(qqEvent);
                    break;
                case CAP:
                    capUp(qqEvent);
                    break;
                case VFY:
                    vfyUp(qqEvent);
                    break;
                case NEWSIG:
                    newSigUp(qqEvent);
                    break;
                case LOGIN:
                    loginUp(qqEvent);
                    break;
                case CHECKSIG:
                    checksigUp(qqEvent);
                    break;
                case SID:
                    sidUp(qqEvent);
                    break;
                case ALONEPAGE:
                    alonePageUp(qqEvent);
                    break;
                case PWDALONE:
                    pwdaloneUp(qqEvent);
                    break;
                case MAILLIST:
                    mailListUp(qqEvent);
                    break;
                case TASKQUEUE:
                    taskQueue(qqEvent);
                    break;
                case MAIL:
                    mailUp(qqEvent);
                    break;
                case PARSETASKQUEUE:
                    parseTaskQueue(qqEvent);
                    break;
                case PARSE:
                    parseMail(qqEvent);
                    break;
                case ERROR:
                    errorHandle(qqEvent);
                    break;
            }
        } catch (Exception e) {
            logger.error("request up error {}", qqEvent.getId(), e);
            qqEvent.setException(e);
            errorHandle(event);
        }
    }

    @Override
    protected void setHeader(String uriStr, HttpMessage httpGet,
                             MailSpiderEvent event) {
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("User-Agent", USERAGENT_CHROME);
        try {
            CookieHelper.setCookies(uriStr, httpGet, event.getCookieList());
        } catch (URISyntaxException e) {
            logger.error("set cookie fail {}", event.getId());
        }
    }

    /**
     * get vcode
     *
     * @param event event
     */
    private void checkUp(MailQQSpiderEvent event) {
        logger.debug("do checkup {}", event.getId());
        String s = String.format(checkUrl, event.getAccount(), Math.random());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpGet.setHeader("Referer", refererUrl);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * get cap
     *
     * @param event event
     */
    private void showUp(MailQQSpiderEvent event) {
        logger.debug("do showup {}", event.getId());
        String s = String.format(capShowUrl, event.getAccount(),
                event.getCapCd(), Math.random());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * get vcode
     *
     * @param event event
     */
    private void capUp(MailQQSpiderEvent event) {
        logger.debug("do capup {}", event.getId());
        String s = String.format(capImgUrl, event.getAccount(), event.getSig());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * verify vcode
     *
     * @param event event
     */
    private void vfyUp(MailQQSpiderEvent event) {
        logger.debug("do vfy vcode up {}", event.getId());
        String s = String.format(vfyCapUrl, event.getAccount(),
                event.getvCode(), event.getSig());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * get new sig
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: newSigUp
     * @date 2015年10月21日 下午4:41:43
     * @author yukf
     */
    private void newSigUp(MailQQSpiderEvent event) {
        logger.debug("do new sig up {}", event.getId());
        String s = String.format(newSigUrl, event.getAccount(), event.getSig(),
                Math.random());
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * login
     *
     * @param event event
     */
    private void loginUp(MailQQSpiderEvent event) {
        logger.debug("do login passwrod up {}", event.getId());
        String password;
        String randStr = event.getRandStr();
        String salt = event.getSalt();
        try {
            password = ScriptHelper.encryptQQPassword(event.getPassword(),
                    salt, randStr);
        } catch (Exception e) {
            logger.error("encrypt password:{} error {}", event.getPassword(),
                    event.getId(), e);
            event.setException(e);
            return;
        }
        String s = String.format(loginUrl, event.getNeedCode(), event.getSig(),
                randStr, event.getAccount(), password, redirectUrl, 0/*
                                                                     * event.
																	 * getNeedCode
																	 * ()
																	 */);
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpGet.setHeader("Referer", refererUrl);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * do after login
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: checksigUp
     * @date 2015年10月19日 下午3:30:03
     * @author yukf
     */
    private void checksigUp(MailQQSpiderEvent event) {
        logger.debug("do check sig up {}", event.getId());
        HttpGet httpGet = new HttpGet(event.getCheckSigUrl());
        setHeader(event.getCheckSigUrl(), httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * judge if need extrapwd
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: sidUp
     * @date 2015年10月19日 下午3:31:42
     * @author yukf
     */
    private void sidUp(MailQQSpiderEvent event) {
        logger.debug("do sid up {}", event.getId());
        HttpGet httpGet = new HttpGet(sidUrl);
        setHeader(sidUrl, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));

    }

    /**
     * visit alonePage
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: alonePageUp
     * @date 2015年10月19日 下午5:00:53
     * @author yukf
     */
    private void alonePageUp(MailQQSpiderEvent event) {
        logger.debug("do get login page up {}, url: {}", event.getId(),
                event.getAlonePageUrl());
        HttpGet httpGet = new HttpGet(event.getAlonePageUrl());
        setHeader(event.getAlonePageUrl(), httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    /**
     * vfy extrapwd
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: pwdaloneUp
     * @date 2015年10月19日 下午3:03:33
     * @author yukf
     */
    private void pwdaloneUp(MailQQSpiderEvent event) {
        logger.debug("do login extra password up {}", event.getId());
        String pwdalone;
        try {
            pwdalone = ScriptHelper.encryptQQAlonePwd(event.getExtraPwd(),
                    event.getTs());
        } catch (Exception e) {
            logger.error("encrypt pwdalone:{} error {}", event.getExtraPwd(),
                    event.getId(), e);
            event.setException(e);
            return;
        }
        HttpPost httpPost = new HttpPost(aloneLoginUrl);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(createFormEntity(
                    pwdalone, event), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("encode body error {}", event.getId(), e);
            event.setException(e);
            return;
        }
        setHeader(aloneLoginUrl, httpPost, event);
        httpPost.setHeader("Referer", event.getAlonePageUrl());
        httpAsyncClient.execute(httpPost, new HttpAsyncCallback(event));

    }

    /**
     * spider maillist
     *
     * @param event event
     */
    private void mailListUp(MailQQSpiderEvent event) {
        logger.debug("do get mail list up {}", event.getId());
        int curPage = event.getCurPage().incrementAndGet();
        String s = String.format(cgiMailListUrl, Math.random(), event.getSid(),
                curPage, ENVELOPE_PER_PAGE);
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    private void taskQueue(MailQQSpiderEvent event) {
        String key = String
                .format(Constant.MAILQQTASKQUEUE, event.getAccount());
        String value = redis.getStringByKey(key);
        if (StringUtils.isNotBlank(value)) {
            SpiderException exception = new SpiderException(1026, "该邮箱已经在导入中");
            event.getDeferredResult().setResult(exception.toString());
            return;
        }
        redis.set(key, INQUEUE, 600);
        event.setState(MailQQSpiderState.MAIL);
        super.taskQueue(event);
    }

    /**
     * spider mail
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: mailUp
     * @date 2015年10月20日 下午4:33:02
     * @author yukf
     */
    public void mailUp(MailQQSpiderEvent event) {
        logger.debug("do get mail up {}", event.getId());

        if (event.getMailDone().get() == 0
                && event.getTicTac().get("spider_mail") == null)
            event.getTicTac().tic("spider_mail");

        Envelope envelope = event.getEnvelopes().poll();
        String mailid = envelope.getId();
        String s = String
                .format(mailUrl, Math.random(), event.getSid(), mailid);
        HttpGet httpGet = new HttpGet(s);
        setHeader(s, httpGet, event);
        httpAsyncClient.execute(httpGet, new HttpAsyncCallback(event));
    }

    private void parseTaskQueue(MailQQSpiderEvent event) {
        event.setState(MailQQSpiderState.PARSE);
        super.parseTaskQueue(event);
    }

    /**
     * parse mail
     *
     * @param event
     * @Description: (方法职责详细描述, 可空)
     * @Title: parseMail
     * @date 2015年10月21日 下午3:26:24
     * @author yukf
     */
    private void parseMail(MailQQSpiderEvent event) {
        try {
            logger.debug("do parse mail {}", event.getId());
            ParserContext context = new ParserContext();
            context.setUserId(Integer.parseInt(event.getUserid()));
            String account = event.getAccount();
            if (!account.contains("@")) {
                account = account + "@qq.com";
            }
            context.setAccount(account);
            context.setMailSrcs(event.getMailSrcs());
            context.setRedisKey(Constant.mailqqResultKey);
            ParserReactor.getInstance().process(context);

			/*
             * logger.debug("do parse mail {}", event.getId()); MailParser
			 * parser = MailParser.getInstance(); Map<String, List<UserCardVo>>
			 * result = parser.execute(event.getMailSrcs()); SpiderResult
			 * spiderResult = createSpiderResult(event, result); String key =
			 * String.format(Constant.mailqqResultKey, event.getUserid());
			 * redis.set(key, JSON.toJSONString(spiderResult), 300); // expire
			 * in 5minutes logger.debug("parse mail finish {}", event.getId());
			 */
        } finally {
            // 移除INQUEUE状态，让用户可以再导入
            String key = String.format(Constant.MAILQQTASKQUEUE,
                    event.getAccount());
            redis.del(key);
        }
    }

    /**
     * create spider result
     *
     * @param event
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: createSpiderResult
     * @date 2015年10月26日 上午11:36:03
     * @author yukf
     */
    private SpiderResult createSpiderResult(MailQQSpiderEvent event) {
        SpiderResult spiderResult = new SpiderResult();
        spiderResult.setExtraPwd(event.getExtraPwd());
        spiderResult.setAccount(event.getAccount());
        spiderResult.setPassword(event.getPassword());
        return spiderResult;
    }

    /**
     * parse json mail list to envelope list
     *
     * @param event
     * @param mailList
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: parseJsonToEnvelope
     * @date 2015年10月24日 上午9:43:08
     * @author yukf
     */
    private List<Envelope> parseJsonToEnvelope(MailQQSpiderEvent event,
                                               String mailList) {
        List<Envelope> envelopes = new ArrayList<>();
        JSONObject jsonObj = JSON.parseObject(mailList);
        JSONArray idxSt = jsonObj.getJSONArray("idxSt");
        JSONObject cntObj = idxSt.getJSONObject(0);
        if (event.getCurPage().get() == 0) {
            int max = Math.min(cntObj.getIntValue("cnt"), MAX_MAIL_SIZE);
            int maxPage = max / 100;
            if (max % 100 != 0) {
                maxPage++;
            }
            maxPage--;
            event.setMaxPage(maxPage);
        }
        JSONArray mailArr = jsonObj.getJSONArray("mls");

        if (mailArr == null) {
            logger.error("no mls in mails: {}", mailList);
            return envelopes;
        }

        for (int i = 0; i < mailArr.size(); i++) {
            try {
                JSONObject mailObj = null;
                Envelope envelope = new Envelope();
                mailObj = mailArr.getJSONObject(i);
                JSONObject jsonSubject = mailObj.getJSONObject("inf");
                String mailid = jsonSubject.getString("id");
                String subject = jsonSubject.getString("subj");
                Long date = jsonSubject.getLong("date");
                JSONObject fromObj = jsonSubject.getJSONObject("from");
                String from = fromObj.getString("addr");
                JSONArray toArr = jsonSubject.getJSONArray("toLst");
                envelope.setFrom(from);
                envelope.setId(mailid);
                if (toArr.size() > 0) {
                    envelope.setTo(toArr.getJSONObject(0).getString("addr"));
                } else {
                    envelope.setTo("");
                }
                envelope.setSubject(subject);
                envelope.setSentDate(date.toString());
                envelope.setReceiveDate(date.toString());
                envelopes.add(envelope);
            } catch (Exception e) {
                logger.error("parse mail envelope error", e);
            }
        }
        return envelopes;
    }

    /**
     * create form entity when send extrapwd
     *
     * @param event
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: createFormEntity
     * @date 2015年10月20日 上午11:52:35
     * @author yukf
     */
    private List<? extends NameValuePair> createFormEntity(String pwdalone,
                                                           MailQQSpiderEvent event) {
        String spcache = CookieHelper.getSpecCookieValue("spcache",
                event.getCookieList());
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("device", ""));
        params.add(new BasicNameValuePair("ts", event.getTs()));
        params.add(new BasicNameValuePair("p", pwdalone));
        params.add(new BasicNameValuePair("f", "xhtml"));
        params.add(new BasicNameValuePair("delegate_url", ""));
        params.add(new BasicNameValuePair("action", ""));
        params.add(new BasicNameValuePair("https", "true"));
        params.add(new BasicNameValuePair("tfcont", ""));
        params.add(new BasicNameValuePair("uin", event.getAccount()));
        params.add(new BasicNameValuePair("aliastype", "other"));
        params.add(new BasicNameValuePair("pwd", ""));
        params.add(new BasicNameValuePair("spcache", spcache));
        params.add(new BasicNameValuePair("ss", "1"));
        params.add(new BasicNameValuePair("btlogin", "登录"));
        return params;
    }

    /**
     * parse json mail to mailsrc model
     *
     * @param mailJson
     * @param event
     * @return
     * @Description: (方法职责详细描述, 可空)
     * @Title: parseJsonMailToMailSrc
     * @date 2015年10月24日 上午9:43:56
     * @author yukf
     */
    private MailSrc parseJsonMailToMailSrc(String mailJson,
                                           MailQQSpiderEvent event) throws Exception {
        MailSrc mailSrc = new MailSrc();
        JSONObject jsonObj = JSON.parseObject(mailJson);
        JSONArray mlsArr = jsonObj.getJSONArray("mls");
        JSONObject mlsArr0 = mlsArr.getJSONObject(0);
        JSONObject infObj = mlsArr0.getJSONObject("inf");
        String id = infObj.getString("id");
        long date = infObj.getLongValue("date");
        String subj = infObj.getString("subj");
        JSONObject fromObj = infObj.getJSONObject("from");
        String from = fromObj.get("addr").toString();
        mailSrc.setDate(new Date(date * 1000));
        mailSrc.setTitle(subj);
        mailSrc.setFrom(from);
        if (id.length() < MAIL_ID_LEN) {
            // 重叠邮件
            parseOverlapMail(mailSrc, mlsArr);
        } else {
            parseNomalMail(mailSrc, mlsArr);
        }
        return mailSrc;
    }

    /**
     * parse nomal length mailid
     *
     * @param mailSrc
     * @param mlsArr
     * @Description: (方法职责详细描述, 可空)
     * @Title: parseNomalMail
     * @date 2015年10月24日 上午9:44:19
     * @author yukf
     */
    private void parseNomalMail(MailSrc mailSrc, JSONArray mlsArr) {
        JSONObject mlsArr0 = mlsArr.getJSONObject(0);
        JSONObject contentObj = mlsArr0.getJSONObject("content");
        String body = contentObj.getString("body");
        mailSrc.setBody(body);
    }

    /**
     * parse short length mailid
     *
     * @param mailSrc
     * @param mlsArr
     * @Description: (方法职责详细描述, 可空)
     * @Title: parseOverlapMail
     * @date 2015年10月24日 上午9:45:05
     * @author yukf
     */
    private void parseOverlapMail(MailSrc mailSrc, JSONArray mlsArr) {
        JSONObject mlsArr1 = mlsArr.getJSONObject(1);
        JSONObject contentObj = mlsArr1.getJSONObject("content");
        JSONArray foldArr = contentObj.getJSONArray("fold");
        JSONArray inArray = foldArr.getJSONArray(0);
        String body = inArray.getString(1);
        mailSrc.setBody(body);
    }

    /**
     * async call back handler
     */
    private class HttpAsyncCallback implements FutureCallback<HttpResponse> {

        private MailQQSpiderEvent event;
        private boolean skipNextStep = false;

        public HttpAsyncCallback(MailQQSpiderEvent event) {
            this.event = event;
        }

        @Override
        public void completed(HttpResponse result) {
            try {
                // extract cookie
                CookieHelper.getCookies(event.getCookieList(), result);
                switch (event.getState()) {
                    case CHECK:
                        checkDown(result);
                        break;
                    case SHOW:
                        showDown(result);
                        break;
                    case CAP:
                        capDown(result);
                        break;
                    case VFY:
                        vfyDown(result);
                        break;
                    case NEWSIG:
                        newSigDown(result);
                        break;
                    case LOGIN:
                        loginDown(result);
                        break;
                    case CHECKSIG:
                        checksigDown(result);
                        break;
                    case SID:
                        sidDown(result);
                        break;
                    case ALONEPAGE:
                        alonePageDown(result);
                        break;
                    case PWDALONE:
                        pwdaloneDown(result);
                        break;
                    case MAILLIST:
                        mailListDown(result);
                        break;
                    case MAIL:
                        mailDown(result);
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
         * fail
         *
         * @param e exception
         */
        @Override
        public void failed(Exception e) {
            logger.debug("request {} failed: {}", event.getId(), e.getMessage());
            if (event.getState() == MailQQSpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(MailQQSpiderState.PARSETASKQUEUE);
                    onEvent(event);
                }
            }
        }

        /**
         * cancelled
         */
        @Override
        public void cancelled() {
            logger.debug("request cancelled: {}", event.getId());
            if (event.getState() == MailQQSpiderState.MAIL) {
                int done = event.getMailDone().incrementAndGet();
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(MailQQSpiderState.PARSETASKQUEUE);
                    onEvent(event);
                }
            }
        }

        /**
         * get vcode callback
         *
         * @param result http response
         */
        private void checkDown(HttpResponse result) {
            logger.debug("check down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = checkDownPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    String[] strings = group.split(",");
                    if (strings.length != 5) {
                        logger.error(
                                "error vCode response from {} resultStr: {}",
                                event.getId(), resultStr);
                        event.setException(new SpiderException(1008,
                                "CHECK未知数据格式"));
                        return;
                    }

                    String code = strings[0];
                    code = StringUtils.strip(code, "'"); // remove "'"

                    switch (code) {
                        // 不需要验证码
                        case "0": {
                            String randStr = strings[1];
                            randStr = StringUtils.strip(randStr, "'"); // remove "'"
                            event.setRandStr(randStr);

                            String salt = strings[2];
                            salt = StringUtils.strip(salt, "'"); // remove "'"
                            event.setSalt(salt);

                            String sig = strings[3];
                            sig = StringUtils.strip(sig, "'");
                            event.setSig(sig);

                            event.setNeedCode("0");
                            event.setState(MailQQSpiderState.LOGIN);
                            break;
                        }
                        // 需要验证码
                        case "1": {
                            String capCd = strings[1];
                            capCd = StringUtils.strip(capCd, "'"); // remove "'"
                            event.setCapCd(capCd);

                            String salt = strings[2];
                            salt = StringUtils.strip(salt, "'"); // remove "'"
                            event.setSalt(salt);

                            event.setNeedCode("1");
                            event.setState(MailQQSpiderState.SHOW);
                            break;
                        }
                        default:
                            logger.error(
                                    "error vCode response from {} resultStr: {}",
                                    event.getId(), resultStr);
                            event.setException(new SpiderException(1007,
                                    "CHECK未知状态码"));
                            return;
                    }
                } else {
                    logger.error("error vCode response from {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1008, "CHECK未知数据格式"));
                    return;
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        /**
         * check vcode call back
         *
         * @param result http response
         */
        private void showDown(HttpResponse result) {
            logger.debug("show down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = cap_sigPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    event.setSig(group);
                    event.setState(MailQQSpiderState.CAP);
                } else {
                    logger.error("get cap sig error {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1009, "没有获得SIG"));
                    return;
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        /**
         * check vcode call back
         *
         * @param result http response
         */
        private void capDown(HttpResponse result) {
            logger.debug("cap down {}", event.getId());
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
				/*
				 * if (baos != contentLength) { logger.error("get cap error");
				 * event.setException(new SpiderException(1010, "获取验证码错误"));
				 * return; }
				 */

                String imgStr = Base64.getEncoder().encodeToString(rbyte);

                // save to file, used for debug
                if (appConfig.inDevMode()) {
                    byte[] tbytes = Base64.getDecoder().decode(imgStr);
                    // FileOutputStream fs = new
                    // FileOutputStream("/Users/bobo/Desktop/vcode.jpg");
                    FileOutputStream fs = new FileOutputStream(
                            appConfig.getUploadPath() + "/" + event.getUserid()
                                    + ".jpg");
                    fs.write(tbytes);
                    fs.close();
                }

                // store event to redis
                event.setState(MailQQSpiderState.VFY); // next step is to verify
                // vCode
                String key = String.format(Constant.mailqqImportKey,
                        event.getUserid());
                redis.set(key, JSON.toJSONString(event), 300); // expire in 5
                // minutes
                logger.debug("need vfy code {}", event.getId());
                // return vcode to user
                event.setException(new SpiderException(1001, "输入验证码", imgStr));
            } catch (Exception e) {
                logger.error("get cap exception", e);
                event.setException(e);
            }
        }

        /**
         * verify vcode call back
         *
         * @param result http response
         */
        private void vfyDown(HttpResponse result) {
            logger.debug("vfy down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity(),
                        "UTF-8");
                Matcher matcher = vfyDownPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    MailQQVerifyResult verifyResult = JSON.parseObject(group,
                            MailQQVerifyResult.class);
                    // 验证码正确
                    if (verifyResult.getRcode() == 0) {
                        logger.debug("vfy code correct {}", event.getId());
                        event.setRandStr(verifyResult.getRandstr());
                        event.setSig(verifyResult.getSig());
                        event.setState(MailQQSpiderState.LOGIN);
                    } else {
                        // 验证码错误，返回到NEWSIG
                        logger.debug("vfy code incorrect {}", event.getId());
                        event.setState(MailQQSpiderState.NEWSIG);
                    }
                } else {
                    logger.error("verify result error {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1011, "验证码返回数据格式出错"));
                }
            } catch (Exception e) {
                logger.error("verify result error", e);
                event.setException(e);
            }
        }

        /**
         * new sig call back
         *
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: newSigDown
         * @date 2015年10月21日 下午4:44:33
         * @author yukf
         */
        private void newSigDown(HttpResponse result) {
            logger.debug("new sig down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = newSigDownPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    event.setSig(group);
                    event.setState(MailQQSpiderState.CAP);
                } else {
                    logger.error("get new sig error {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1025, "获取new sig错误"));
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        /**
         * login call back
         *
         * @param result http response
         */
        private void loginDown(HttpResponse result) {
            logger.debug("login down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = loginDownPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    String[] strings = group.split(",");
                    if (strings.length != 6) {
                        logger.error("error login response {} resultStr: {}",
                                event.getId(), resultStr);
                        event.setException(new SpiderException(1012,
                                "LOGIN未知数据格式"));
                        return;
                    }

                    String code = strings[0];
                    code = StringUtils.strip(code, "'"); // remove "'"
                    String checkSigUrl = strings[2];
                    checkSigUrl = StringUtils.strip(checkSigUrl, "'"); // remove
                    // "'"
                    switch (code) {
                        case "0":
                            logger.debug("password right {}", event.getId());
                            event.setCheckSigUrl(checkSigUrl);
                            event.setState(MailQQSpiderState.CHECKSIG);
                            return;
                        default:
                            logger.debug("password incorrect {}", event.getId());
                            String message = strings[4];
                            message = StringUtils.strip(message, "'"); // remove "'"
                            event.setException(new SpiderException(1013, message));
                            return;
                    }
                } else {
                    logger.error("error login response {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1008, "LOGIN未知数据格式"));
                    return;
                }
            } catch (Exception e) {
                logger.error("login exception", e);
                event.setException(e);
            }
        }

        /**
         * check sig call back
         *
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: checksigDown
         * @date 2015年10月19日 下午4:31:09
         * @author yukf
         */
        private void checksigDown(HttpResponse result) {
            logger.debug("check sig down {}", event.getId());
            event.setState(MailQQSpiderState.SID);
        }

        /**
         * sid call back
         *
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: sidDown
         * @date 2015年10月19日 下午4:30:58
         * @author yukf
         */
        private void sidDown(HttpResponse result) {
            logger.debug("sid down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = sidDownPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    // 不需要独立密码
                    if (group.contains("today?sid")) {
                        String sid = CookieHelper.getSpecCookieValue("msid",
                                event.getCookieList());
                        event.setSid(sid);
                        logger.debug("do not need extra password {}",
                                event.getId());
                        event.setState(MailQQSpiderState.MAILLIST);
                        return;
                    }
                    // 需要独立密码
                    logger.debug("need extra password {}", event.getId());
                    event.setAlonePageUrl(group);
                    event.setState(MailQQSpiderState.ALONEPAGE);
                    return;
                } else {
                    logger.error("get url error {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1017, "获取url错误"));
                    return;
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }

        }

        /**
         * visit alone page call back
         *
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: alonePageDown
         * @date 2015年10月20日 上午9:33:19
         * @author yukf
         */
        private void alonePageDown(HttpResponse result) {
            logger.debug("alone page down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = alonePageDownPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    event.setTs(group);
                    if (StringUtils.isNotEmpty(event.getExtraPwd())) {
                        event.setState(MailQQSpiderState.PWDALONE);// 如果有独立密码了，直接去登录
                    } else {
                        // 不然就提示用户输入
                        String key = String.format(Constant.mailqqImportKey,
                                event.getUserid());
                        redis.set(key, JSON.toJSONString(event), 300); // expire
                        // in 5
                        // minutes
                        event.setException(new SpiderException(1014, "输入独立密码"));
                    }
                } else {
                    logger.error("get ts {} resultStr: {}", event.getId(),
                            resultStr);
                    event.setException(new SpiderException(1015, "没有获得ts"));
                }
            } catch (Exception e) {
                event.setException(e);
            }
        }

        /**
         * vfy alone pwd call back
         *
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: pwdaloneDown
         * @date 2015年10月19日 下午4:31:05
         * @author yukf
         */
        private void pwdaloneDown(HttpResponse result) {
            logger.debug("pwdalone down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                Matcher matcher = sidDownPattern.matcher(resultStr);
                if (matcher.find()) {
                    String group = matcher.group();
                    // 独立密码正确
                    if (group.contains("today?sid")) {
                        Matcher sidMatcher = getsidPattern.matcher(group);
                        if (sidMatcher.find()) {
                            logger.debug("extra password correct {}",
                                    event.getId());
                            String sid = sidMatcher.group();
                            event.setSid(sid);
                            event.setState(MailQQSpiderState.MAILLIST);
                            return;
                        }
                        logger.error("get sid error {} resultStr: {}",
                                event.getId(), resultStr);
                        event.setException(new SpiderException(1018, "获取sid错误"));
                        return;

                    }
                    logger.info("extra password incorrect {}", event.getId());
                    // 不然就提示用户输入
                    String key = String.format(Constant.mailqqImportKey,
                            event.getUserid());
                    redis.set(key, JSON.toJSONString(event), 300); // expire in
                    // 5 minutes
                    // 独立密码不正确
                    event.setException(new SpiderException(1016, "独立密码错误"));
                    return;
                } else {
                    logger.error("get url error {} resultStr: {}",
                            event.getId(), resultStr);
                    event.setException(new SpiderException(1017, "获取url错误"));
                    return;
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        /**
         * spider mail list call back
         *
         * @param result http response
         */
        private void mailListDown(HttpResponse result) {
            logger.debug("mail list down {}", event.getId());
            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                List<Envelope> envelopes = parseJsonToEnvelope(event, resultStr);
                // 还要继续获取下一个maillist
                if (event.getCurPage().get() < event.getMaxPage()) {
                    event.getEnvelopes().addAll(envelopes);
                    event.setState(MailQQSpiderState.MAILLIST);
                    return;
                }
                ConcurrentLinkedDeque<Envelope> hasEnve = event.getEnvelopes();
                int has = hasEnve.size();
                int tsize = envelopes.size();
                int totalMail = Math.min(has + tsize, MAX_MAIL_SIZE);
                int left = totalMail - has;
                for (int i = 0; i < left; i++) {
                    hasEnve.addLast(envelopes.get(i));
                }

                int size = hasEnve.size();
                hasEnve = ParserReactor.getInstance().envelopesFilter(hasEnve, event.getDate());
                event.setEnvelopes(hasEnve);

                logger.info("filter mail, id:{}, account: {}, date: {}, before: {}, after: {}",
                        event.getId(), event.getAccount(), new Date(event.getDate()), size, hasEnve.size());

                if (hasEnve.size() > 0) {
                    logger.info("get mail list done, need to download {} mails, id: {}, account: {}", hasEnve.size(),
                            event.getId(), event.getAccount());
                    event.setTotalMail(hasEnve.size());
                    event.setState(MailQQSpiderState.TASKQUEUE);
                } else {
                    // 完成，返回结果
                    SpiderResult spiderResult = createSpiderResult(event);
                    String key = String.format(Constant.mailqqResultKey,
                            event.getUserid());
                    Map<String, BillResult> billResultMap = new HashMap<>();
                    redis.set(key, JSON.toJSONString(billResultMap), 300);
                    //redis.set(key, JSON.toJSONString(spiderResult), 300); // expire
                    
					
					// in
                    // 5
                    // minutes
                    event.getDeferredResult().setResult(
                            new SpiderException(1019, "开始获取邮件").toString());
                    logger.info("get mail list done, do not need to download mails, id: {}, account: {}",
                            event.getId(), event.getAccount());
                    skipNextStep = true;
                }

            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            }
        }

        /**
         * get mail call back
         *
         * @param result
         * @Description: (方法职责详细描述, 可空)
         * @Title: mailDown
         * @date 2015年10月20日 下午4:33:35
         * @author yukf
         */
        private void mailDown(HttpResponse result) {
            logger.debug("mail down {}", event.getId());

            try {
                String resultStr = EntityUtils.toString(result.getEntity());
                int done = event.getMailDone().incrementAndGet();
                try {
                    MailSrc mailSrc = parseJsonMailToMailSrc(resultStr, event);
                    event.getMailSrcs().add(mailSrc);
                    if (appConfig.inDevMode()) {
                        File parent = new File(appConfig.getUploadPath()
                                + "/mail/");
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        writeToFile(appConfig.getUploadPath() + "/mail/",
                                mailSrc.getTitle() + Math.random() + ".html",
                                mailSrc.getBody());
                    }
                } catch (Exception e1) {
                    logger.error("error parse json mail to mailsrc {} resultStr: {}",
                            event.getId(), resultStr);
                }
                if (done == event.getTotalMail()) {
                    logger.debug("get mail done {}", event.getId());
                    event.setState(MailQQSpiderState.PARSETASKQUEUE);
                } else {
                    skipNextStep = true;
                }
            } catch (Exception e) {
                logger.error("get result exception", e);
                event.setException(e);
            } finally {
                if (event.getMailDone().get() == event.getTotalMail()) {
                    event.getTicTac().tac("spider_mail");
                    logger.info("spider mail spend {}", event.getTicTac()
                            .elapsed("spider_mail"));
                }
            }
        }

    }
}
