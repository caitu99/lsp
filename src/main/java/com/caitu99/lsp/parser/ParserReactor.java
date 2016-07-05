package com.caitu99.lsp.parser;

import com.alibaba.fastjson.JSON;
import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.cache.RedisOperate;
import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.BillResult;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.parser.bank.BankBill;
import com.caitu99.lsp.model.parser.shop.ShopBill;
import com.caitu99.lsp.model.parser.travel.TravelBill;
import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.model.spider.hotmail.HotmailItem;
import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.mongodb.model.MGLog;
import com.caitu99.lsp.mongodb.parser.MGBillService;
import com.caitu99.lsp.mongodb.parser.MGLogService;
import com.caitu99.lsp.parser.utils.TplHelper;
import com.caitu99.lsp.utils.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ParserReactor {

    private final static Logger logger = LoggerFactory
            .getLogger(ParserReactor.class);

    private static ParserReactor reactor = new ParserReactor();

    private AppConfig appConfig = SpringContext.getBean(AppConfig.class);
    private RedisOperate redis;

    private volatile boolean isStopping = true;

    private ThreadPoolExecutor executor;

    private Map<String, ICard> allCardMap;
    private List<Class> allTpls = new ArrayList<>();

    private MGBillService mgBillService;
    private MGLogService mgLogService;

    private ParserReactor() {
        redis = SpringContext.getBean(RedisOperate.class);
        mgBillService = SpringContext.getBean(MGBillService.class);
        mgLogService = SpringContext.getBean(MGLogService.class);

        // newCachedThreadPool
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        executor.prestartAllCoreThreads();

        Map<String, List<Class>> allTplMap = TplHelper.getTplClzz();
        allCardMap = TplHelper.getAllCard();

        for (Map.Entry<String, List<Class>> entry : allTplMap.entrySet())
            allTpls.addAll(entry.getValue());

        // initialize tpls of card
        initCardTpls(allCardMap, allTplMap);
    }

    public static ParserReactor getInstance() {
        return reactor;
    }

    public void process(ParserContext context) {
        try {
            switch (context.getStatus()) {
                case HEAD: {
                    Collection<MailSrc> mailSrcs = context.getMailSrcs();
                    for (MailSrc mailSrc : mailSrcs) {
                        String title = mailSrc.getTitle();
                        List<Class> tpls = null;
                        for (Map.Entry<String, ICard> entry : allCardMap.entrySet()) {
                            ICard card = entry.getValue();
                            if (card.is(title, mailSrc.getFrom())) {
                                tpls = card.getTpls();
                                break;
                            }
                        }
                        if (tpls == null) {
                            logger.error("can not get template: {}", mailSrc);
                            continue;
                        }

                        // commit a task to parse
                        TplTask task = new TplTask(context, tpls);
                        Runnable runnable = () -> task.process(mailSrc);
                        executor.submit(runnable);
                    }
                    break;
                }
                case BODY: {
                    // swap mailSrcs and unparsed mailSrcs
                    context.getMailSrcs().clear();
                    context.getMailSrcs().addAll(context.getUnparsedMailSrcs());
                    context.getUnparsedMailSrcs().clear();
                    context.getMailSrcCount().set(0);

                    Collection<MailSrc> unparsedMailSrcs = context.getMailSrcs();
                    for (MailSrc mailSrc : unparsedMailSrcs) {
                        TplTask task = new TplTask(context, allTpls);
                        Runnable runnable = () -> task.process(mailSrc);
                        executor.submit(runnable);
                    }
                    break;
                }
                case MERGE: {
                    // merge result
                    List<Bill> bills = context.getBills();
                    for (Bill bill : bills) {
                        try {
                            ICard card = bill.getTpl().getCard();
                            card.merge(bill);
                        } catch (Exception e) {
                            logger.error("merge error, context: {}, src: {}", context, bill, e);
                        }
                    }

                    // process unparsed
                    Collection<MailSrc> unparsedMailSrcs = context
                            .getUnparsedMailSrcs();
                    for (MailSrc mailSrc : unparsedMailSrcs) {
                        MGBill mgBill = new MGBill();

                        mgBill.setUserId(context.getUserId());
                        mgBill.setAccount(context.getAccount());
                        mgBill.setTitle(mailSrc.getTitle());
                        mgBill.setsDate(mailSrc.getDate());
                        mgBill.setStatus(2);

                        MGBill unparsedBill = mgBillService.getUnparsed(mgBill);
                        if (unparsedBill != null) {
                            MGLog mgLog = new MGLog();
                            mgLog.setbId(unparsedBill.getId());
                            mgLog.setContextId(context.getId());
                            mgLog.setSrcId(mailSrc.getId());
                            mgLog.setCreated(new Date());
                            mgLogService.insert(mgLog);
                            logger.info("update unparsed in db: {}", mailSrc);
                        } else {
                            mgBill.setContextId(context.getId());
                            mgBill.setSrcId(mailSrc.getId());
                            mgBill.setBody(mailSrc.getBody());
                            mgBill.setCreated(new Date());
                            mgBillService.insert(mgBill);

                            MGLog mgLog = new MGLog();
                            mgLog.setbId(mgBill.getId());
                            mgLog.setContextId(context.getId());
                            mgLog.setSrcId(mailSrc.getId());
                            mgLog.setCreated(new Date());
                            mgLogService.insert(mgLog);
                            logger.info("store unparsed to db: {}", mailSrc);
                        }
                    }

                    // set result
                    // Key = "1_中国银行信用卡_李华君"
                    // Key = "1_中国银行信用卡_李华君_卡号"
                    // Key = "1_艺龙_账号"
                    Map<String, BillResult> billResultMap = new HashMap<>();
                    for (Bill bill : bills) {
                        ICard card = bill.getTpl().getCard();
                        BillResult billResult = new BillResult();
                        billResult.setBillMonth(bill.getBillDay());
                        billResult.setIntegral(bill.getIntegral());
                        billResult.setName(bill.getName());
                        billResult.setOthers(bill.getOthers());
                        billResult.setUserId(context.getUserId());
                        billResult.setCardName(card.getName());
                        billResult.setCardTypeId(bill.getId());

                        StringBuilder key = new StringBuilder();
                        key.append(billResult.getUserId()).append("_")
                                .append(card.getName()).append("_")
                                .append(bill.getName());

                        if (bill instanceof BankBill) {
                            BankBill bankBill = (BankBill) bill;
                            billResult.setAlone(bankBill.isAlone());
                            billResult.setCardNo(bankBill.getCardNo());

                            if (bankBill.isAlone()) // 积分独立
                                key.append("_").append(bankBill.getCardNo());

                        } else if (bill instanceof TravelBill) {
                            TravelBill travelBill = (TravelBill) bill;
                            billResult.setAlone(false);
                            billResult.setCardNo(travelBill.getCardNo());
                        } else if (bill instanceof ShopBill) {

                        }
                        BillResult tempBillResult = billResultMap.get(key
                                .toString());
                        if (tempBillResult == null
                                || tempBillResult.getBillMonth().getTime() < billResult
                                .getBillMonth().getTime()) {// 保存到map中
                            billResultMap.put(key.toString(), billResult);
                        }
                    }
                    String key = String.format(context.getRedisKey(), context.getUserId());
                    logger.info("set result for: {}, result is: {}", context, JSON.toJSONString(billResultMap));
                    redis.set(key, JSON.toJSONString(billResultMap), 300);

                    break;
                }
                default: {
                    logger.error("unknown status: {}", context.getId());
                }
            }
        } catch (Exception e) {
            logger.error("process error", e);
        }
    }

    public void processDebug(ParserContext context) {
    }

    private void initCardTpls(Map<String, ICard> allCard,
                              Map<String, List<Class>> allTpl) {
        for (Map.Entry<String, ICard> entry : allCard.entrySet()) {
            entry.getValue().setTpls(allTpl.get(entry.getKey()));
        }
    }

    public ConcurrentLinkedDeque<Envelope> envelopesFilter(
            ConcurrentLinkedDeque<Envelope> envelopes, long searchTime) {
        ConcurrentLinkedDeque<Envelope> bankEnvelopes = new ConcurrentLinkedDeque<Envelope>();
        for (Envelope envelope : envelopes) {
            long mailDate = Long.parseLong(envelope.getSentDate()) * 1000;
            String subject = envelope.getSubject().trim();
            if (checkEnvelopTitle(subject)) {
                ICard card = getCardByTitleAndSrc(envelope.getSubject(),
                        envelope.getFrom());
                if (card != null) {
                    if (mailDate >= searchTime) {
                        bankEnvelopes.add(envelope);
                    }
                }
            }
        }
        return bankEnvelopes;
    }

    public ConcurrentLinkedDeque<HotmailItem> envelopesFilterForHotMail(
            ConcurrentLinkedDeque<HotmailItem> hotmailItems, long searchTime) {
        ConcurrentLinkedDeque<HotmailItem> bankEnvelopes = new ConcurrentLinkedDeque<HotmailItem>();
        for (HotmailItem hotmailItem : hotmailItems) {
            long mailDate = Long.parseLong(hotmailItem.getTimeLong());
            String subject = hotmailItem.getTitle().trim();
            if (checkEnvelopTitle(subject)) {
                ICard card = getCardByTitleAndSrc(hotmailItem.getTitle(),
                        hotmailItem.getSender());
                if (card != null) {
                    if (mailDate >= searchTime) {
                        bankEnvelopes.add(hotmailItem);
                    }
                }
            }
        }
        return bankEnvelopes;
    }

    private ICard getCardByTitleAndSrc(String title, String sender) {
        for (Map.Entry<String, ICard> entry : allCardMap.entrySet()) {
            if (entry.getValue().is(title.replaceAll(" ","").replaceAll("\\u00A0",""), sender)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean checkEnvelopTitle(String subject) {
        List<String> envelopKeys = appConfig.getEnvelopKeys();
        for (String key : envelopKeys) {
            if (subject.contains(key))
                return true;
        }
        return false;
    }

    public void start() {
        isStopping = false;
    }

    public void stop() {
        isStopping = true;
    }

}
