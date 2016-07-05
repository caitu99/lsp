package com.caitu99.lsp.spider;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.model.spider.airchina.AirChinaSpiderEvent;
import com.caitu99.lsp.model.spider.botaohui.BoTaoHuiEvent;
import com.caitu99.lsp.model.spider.ccbishop.CCBIShopEvent;
import com.caitu99.lsp.model.spider.ccbshop.CCBShopEvent;
import com.caitu99.lsp.model.spider.citybank.CityBankEvent;
import com.caitu99.lsp.model.spider.cmbchina.CmbChinaSpiderEvent;
import com.caitu99.lsp.model.spider.cmishop.CMIShopEvent;
import com.caitu99.lsp.model.spider.comishop.BocomMileageEvent;
import com.caitu99.lsp.model.spider.comishop.COMIShopEvent;
import com.caitu99.lsp.model.spider.csair.CsairSpiderEvent;
import com.caitu99.lsp.model.spider.hotmail.MailHotmailSpiderEvent;
import com.caitu99.lsp.model.spider.ihghotel.IHGHotelEvent;
import com.caitu99.lsp.model.spider.jingdong.JingDongEvent;
import com.caitu99.lsp.model.spider.liantong.LianTongSpiderEvent;
import com.caitu99.lsp.model.spider.liantong.UnicomChinaSpiderEvent;
import com.caitu99.lsp.model.spider.mail126.Mail126SpiderEvent;
import com.caitu99.lsp.model.spider.mail139.Mail139SpiderEvent;
import com.caitu99.lsp.model.spider.mail163.Mail163SpiderEvent;
import com.caitu99.lsp.model.spider.mailqq.MailQQSpiderEvent;
import com.caitu99.lsp.model.spider.mailsina.MailSinaSpiderEvent;
import com.caitu99.lsp.model.spider.pufabank.PufaBankEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnOilSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnSpiderEvent;
import com.caitu99.lsp.model.spider.pingan.PingAnxykSpiderEvent;
import com.caitu99.lsp.model.spider.taobao.TaoBaoSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiShopSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.TianYiSpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.YJF189SpiderEvent;
import com.caitu99.lsp.model.spider.tianyi.YJF189ntSpiderEvent;
import com.caitu99.lsp.model.spider.unicomshop.UnicomShopEvent;
import com.caitu99.lsp.model.spider.wumart.WumartSpiderEvent;
import com.caitu99.lsp.model.spider.yidong.YiDongEvent;
import com.caitu99.lsp.spider.airchina.AirChinaBodySpider;
import com.caitu99.lsp.spider.airchina.AirChinaSpider;
import com.caitu99.lsp.spider.botaohui.BoTaoHuiSpider;
import com.caitu99.lsp.spider.ccb.CCBSpider;
import com.caitu99.lsp.spider.ccbi.CCBISpider;
import com.caitu99.lsp.spider.citybank.CityBankSpider;
import com.caitu99.lsp.spider.cmbchina.CmbChinaBodySpider;
import com.caitu99.lsp.spider.cmbchina.CmbChinaSpider;
import com.caitu99.lsp.spider.cmi.CMISpider;
import com.caitu99.lsp.spider.comi.BocomMileageSpider;
import com.caitu99.lsp.spider.comi.COMISpider;
import com.caitu99.lsp.spider.csair.CsairBodySpider;
import com.caitu99.lsp.spider.csair.CsairSpider;
import com.caitu99.lsp.spider.hotmail.MailHotmailBodySpider;
import com.caitu99.lsp.spider.hotmail.MailHotmailSpider;
import com.caitu99.lsp.spider.ihghotel.IHGHotelSpider;
import com.caitu99.lsp.spider.jingdong.JingDongSpider;
import com.caitu99.lsp.spider.liantong.LianTongBodySpider;
import com.caitu99.lsp.spider.liantong.LianTongSpider;
import com.caitu99.lsp.spider.liantong.UnicomChinaSpider;
import com.caitu99.lsp.spider.mail126.Mail126BodySpider;
import com.caitu99.lsp.spider.mail126.Mail126Spider;
import com.caitu99.lsp.spider.mail139.Mail139BodySpider;
import com.caitu99.lsp.spider.mail139.Mail139Spider;
import com.caitu99.lsp.spider.mail163.Mail163BodySpider;
import com.caitu99.lsp.spider.mail163.Mail163Spider;
import com.caitu99.lsp.spider.mailqq.MailQQBodySpider;
import com.caitu99.lsp.spider.mailqq.MailQQSpider;
import com.caitu99.lsp.spider.mailsina.MailSinaBodySpider;
import com.caitu99.lsp.spider.mailsina.MailSinaSpider;
import com.caitu99.lsp.spider.pufabank.PufaBankSpider;
import com.caitu99.lsp.spider.pingan.PingAnOilSpider;
import com.caitu99.lsp.spider.pingan.PingAnSpider;
import com.caitu99.lsp.spider.pingan.PingAnxykSpider;
import com.caitu99.lsp.spider.taobao.TaoBaoBodySpider;
import com.caitu99.lsp.spider.taobao.TaobaoSpider;
import com.caitu99.lsp.spider.tianyi.TianYiShopSpider;
import com.caitu99.lsp.spider.tianyi.TianYiSpider;
import com.caitu99.lsp.spider.tianyi.YJF189Spider;
import com.caitu99.lsp.spider.tianyi.YJF189ntSpider;
import com.caitu99.lsp.spider.unicomshopspider.UnicomShopSpider;
import com.caitu99.lsp.spider.wumart.WumartBodySpider;
import com.caitu99.lsp.spider.wumart.WumartSpider;
import com.caitu99.lsp.spider.yidong.YiDongSpider;
import com.caitu99.lsp.utils.SpringContext;

/**
 * singleton thread runner
 *
 * @author yukf
 * @Description: (类职责详细描述, 可空)
 * @ClassName: Reactor
 * @date 2015年10月24日 上午9:54:04
 * @Copyright (c) 2015-2020 by caitu99
 */
public class SpiderReactor {

    private final static Logger logger = LoggerFactory.getLogger(SpiderReactor.class);
    private static SpiderReactor reactor = new SpiderReactor();
    public AppConfig appConfig = SpringContext.getBean(AppConfig.class);
    private volatile boolean isStopping = true;

    private IMailSpider mailQQSpider;
    private IMailSpider mail163Spider;
    private IMailSpider maill26Spider;
    private IMailSpider maill39Spider;
    private IMailSpider mailSinaSpider;
    private IMailSpider airChinaSpider;
    private IMailSpider csairSpider;
    private IMailSpider wumartSpider;
    private IMailSpider cmbChinaSpider;
    private QuerySpider jingDongSpider;
    private QuerySpider yiDongSpider;
    private QuerySpider cityBankSpider;
    private QuerySpider ihgHotelSpider;
    private CMISpider cmiSpider;
    private CCBISpider ccbiSpider;
    private COMISpider comiSpider;
    private QuerySpider boTaoHuiSpider;
    private IMailSpider taobaoSpider;
    private QuerySpider tianYiSpider;
    private IMailSpider lianTongSpider;
    private IMailSpider hotmailSpider;
    private UnicomShopSpider unicomShopSpider;
    private CCBSpider ccbSpider;
    private QuerySpider tianYiShopSpider;
    private QuerySpider yJF189Spider;
    private QuerySpider yJF189ntSpider;
    private QuerySpider unicomChinaSpider;
    private QuerySpider pufaBankSpider;
    
    private QuerySpider pinganSpider;
    private QuerySpider pinganOilSpider;
    private QuerySpider pinganxykSpider;
    
    private QuerySpider bocomMileageSpider;

    private ThreadPoolExecutor executor;

    private SpiderReactor() {
        // newCachedThreadPool
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        executor.prestartAllCoreThreads();

        MailQQBodySpider qqBodySpider = new MailQQBodySpider();
        Mail163BodySpider l63BodySpider = new Mail163BodySpider();
        Mail126BodySpider l26BodySpider = new Mail126BodySpider();
        Mail139BodySpider l39BodySpider = new Mail139BodySpider();
        MailSinaBodySpider sinaBodySpider = new MailSinaBodySpider();
        AirChinaBodySpider airChinaBodySpider = new AirChinaBodySpider();
        CmbChinaBodySpider cmbChinaBodySpider = new CmbChinaBodySpider();
        CsairBodySpider csairBodySpider = new CsairBodySpider();
        WumartBodySpider wumartBodySpider = new WumartBodySpider();
        TaoBaoBodySpider taobaoBodySpider = new TaoBaoBodySpider();
        LianTongBodySpider lianTongBodySpider = new LianTongBodySpider();
        MailHotmailBodySpider hotmailBodySpider = new MailHotmailBodySpider();


        MailParserTask mailqqParser = new MailParserTask();
        MailParserTask mail163Parser = new MailParserTask();
        MailParserTask mail126Parser = new MailParserTask();
        MailParserTask mail139Parser = new MailParserTask();
        MailParserTask mailSinaParser = new MailParserTask();
        MailParserTask airChinaParser = new MailParserTask();
        MailParserTask cmbChinaParser = new MailParserTask();
        MailParserTask csairParser = new MailParserTask();
        MailParserTask wumartParser = new MailParserTask();
        MailParserTask taobaoParser = new MailParserTask();
        MailParserTask lianTongParser = new MailParserTask();
        MailParserTask hotmailParser = new MailParserTask();

        mailQQSpider = new MailQQSpider(qqBodySpider, mailqqParser);
        mail163Spider = new Mail163Spider(l63BodySpider, mail163Parser);
        maill26Spider = new Mail126Spider(l26BodySpider, mail126Parser);
        maill39Spider = new Mail139Spider(l39BodySpider, mail139Parser);
        mailSinaSpider = new MailSinaSpider(sinaBodySpider, mailSinaParser);
        airChinaSpider = new AirChinaSpider(airChinaBodySpider, airChinaParser);
        csairSpider = new CsairSpider(csairBodySpider, csairParser);
        wumartSpider = new WumartSpider(wumartBodySpider, wumartParser);
        cmbChinaSpider = new CmbChinaSpider(cmbChinaBodySpider, cmbChinaParser);
        jingDongSpider = new JingDongSpider();
        yiDongSpider = new YiDongSpider();
        cityBankSpider = new CityBankSpider();
        ihgHotelSpider = new IHGHotelSpider();
        cmiSpider = new CMISpider();
        ccbiSpider = new CCBISpider();
        comiSpider = new COMISpider();
        boTaoHuiSpider = new BoTaoHuiSpider();
        taobaoSpider = new TaobaoSpider(taobaoBodySpider, taobaoParser);
        tianYiSpider = new TianYiSpider();
        lianTongSpider = new LianTongSpider(lianTongBodySpider, lianTongParser);
        hotmailSpider = new MailHotmailSpider(hotmailBodySpider, hotmailParser);
        unicomShopSpider = new UnicomShopSpider();
        ccbSpider = new CCBSpider();
        tianYiShopSpider = new TianYiShopSpider();
        yJF189Spider = new YJF189Spider();
        yJF189ntSpider = new YJF189ntSpider();
        unicomChinaSpider = new UnicomChinaSpider();
        pufaBankSpider = new PufaBankSpider();
        pinganSpider = new PingAnSpider();
        pinganOilSpider = new PingAnOilSpider();
        pinganxykSpider = new PingAnxykSpider();
        bocomMileageSpider = new BocomMileageSpider();

        qqBodySpider.setMailSpider(mailQQSpider);
        mailqqParser.setMailSpider(mailQQSpider);
        qqBodySpider.start();
        mailqqParser.start();

        l63BodySpider.setMailSpider(mail163Spider);
        mail163Parser.setMailSpider(mail163Spider);
        l63BodySpider.start();
        mail163Parser.start();

        l26BodySpider.setMailSpider(maill26Spider);
        mail126Parser.setMailSpider(maill26Spider);
        l26BodySpider.start();
        mail126Parser.start();

        l39BodySpider.setMailSpider(maill39Spider);
        mail139Parser.setMailSpider(maill39Spider);
        l39BodySpider.start();
        mail139Parser.start();

        // sina
        sinaBodySpider.setMailSpider(mailSinaSpider);
        mailSinaParser.setMailSpider(mailSinaSpider);
        sinaBodySpider.start();
        mailSinaParser.start();

        // 国航
        airChinaBodySpider.setMailSpider(airChinaSpider);
        airChinaParser.setMailSpider(airChinaSpider);
        airChinaBodySpider.start();
        airChinaParser.start();

        // 招行
        cmbChinaBodySpider.setMailSpider(cmbChinaSpider);
        cmbChinaParser.setMailSpider(cmbChinaSpider);
        cmbChinaBodySpider.start();
        cmbChinaParser.start();

        // 南航
        csairBodySpider.setMailSpider(csairSpider);
        csairParser.setMailSpider(csairSpider);
        csairBodySpider.start();
        csairParser.start();

        //物美
        wumartBodySpider.setMailSpider(wumartSpider);
        wumartParser.setMailSpider(wumartSpider);
        wumartBodySpider.start();
        wumartParser.start();

        //联通
        lianTongBodySpider.setMailSpider(lianTongSpider);
        lianTongParser.setMailSpider(lianTongSpider);
        lianTongBodySpider.start();
        lianTongParser.start();

        //hotmail
        hotmailBodySpider.setMailSpider(hotmailSpider);
        hotmailParser.setMailSpider(hotmailSpider);
        hotmailBodySpider.start();
        hotmailParser.start();

    }

    public static SpiderReactor getInstance() {
        return reactor;
    }

    public void process(Object event) {

        if (event instanceof MailQQSpiderEvent) {
            // for mail qq
            Runnable runnable = () -> mailQQSpider.onEvent((MailQQSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof Mail163SpiderEvent) {
            // for mail 163
            Runnable runnable = () -> mail163Spider.onEvent((Mail163SpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof Mail126SpiderEvent) {
            // for mail 126
            Runnable runnable = () -> maill26Spider.onEvent((Mail126SpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof Mail139SpiderEvent) {
            // for mail 139
            Runnable runnable = () -> maill39Spider.onEvent((Mail139SpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof MailSinaSpiderEvent) {
            // for sina mail
            Runnable runnable = () -> mailSinaSpider.onEvent((MailSinaSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof AirChinaSpiderEvent) {
            // for air china
            Runnable runnable = () -> airChinaSpider.onEvent((AirChinaSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof JingDongEvent) {
            // for jingdong
            Runnable runnable = () -> jingDongSpider.onEvent((JingDongEvent) event);
            executor.execute(runnable);
        } else if (event instanceof YiDongEvent) {
            // for yidong
            Runnable runnable = () -> yiDongSpider.onEvent((YiDongEvent) event);
            executor.execute(runnable);
        } else if (event instanceof CmbChinaSpiderEvent) {
            // for cmb china
            Runnable runnable = () -> cmbChinaSpider.onEvent((CmbChinaSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof CsairSpiderEvent) {
            // for csair
            Runnable runnable = () -> csairSpider.onEvent((CsairSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof WumartSpiderEvent) {
            // for wumart
            Runnable runnable = () -> wumartSpider.onEvent((WumartSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof CityBankEvent) {
            // for citybank
            Runnable runnable = () -> cityBankSpider.onEvent((CityBankEvent) event);
            executor.execute(runnable);
        } else if (event instanceof IHGHotelEvent) {
            Runnable runnable = () -> ihgHotelSpider.onEvent((IHGHotelEvent) event);
            executor.execute(runnable);
        } else if (event instanceof BoTaoHuiEvent) {
            Runnable runnable = () -> boTaoHuiSpider.onEvent((BoTaoHuiEvent) event);
            executor.execute(runnable);
        } else if (event instanceof TaoBaoSpiderEvent) {
            //for taobao
            Runnable runnable = () -> taobaoSpider.onEvent((TaoBaoSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof TianYiSpiderEvent) {
            // for tianyi
            Runnable runnable = () -> tianYiSpider
                    .onEvent((TianYiSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof LianTongSpiderEvent) {
            Runnable runnable = () -> lianTongSpider.onEvent((LianTongSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof MailHotmailSpiderEvent) {
            Runnable runnable = () -> hotmailSpider.onEvent((MailHotmailSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof CMIShopEvent) {
            Runnable runnable = () -> cmiSpider.onEvent((CMIShopEvent) event);
            executor.execute(runnable);
        } else if (event instanceof UnicomShopEvent) {
            Runnable runnable = () -> unicomShopSpider.onEvent((UnicomShopEvent) event);
            executor.execute(runnable);
        } else if (event instanceof CCBIShopEvent) {
            Runnable runnable = () -> ccbiSpider.onEvent((CCBIShopEvent) event);
            executor.execute(runnable);
        } else if (event instanceof COMIShopEvent) {
            Runnable runnable = () -> comiSpider.onEvent((COMIShopEvent) event);
            executor.execute(runnable);
        } else if (event instanceof UnicomShopEvent) {
            Runnable runnable = ()->unicomShopSpider.onEvent((UnicomShopEvent)event);
            executor.execute(runnable);
        } else if (event instanceof CCBShopEvent) {
        	Runnable runnable = ()->ccbSpider.onEvent((CCBShopEvent)event);
        	executor.execute(runnable);
        } else if (event instanceof TianYiShopSpiderEvent) {
            // for tianyishop
            Runnable runnable = () -> tianYiShopSpider
                    .onEvent((TianYiShopSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof YJF189SpiderEvent) {
            // for yjf189  电信用户
            Runnable runnable = () -> yJF189Spider
                    .onEvent((YJF189SpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof YJF189ntSpiderEvent) {
            // for yjf189nt  非电信用户
            Runnable runnable = () -> yJF189ntSpider
                    .onEvent((YJF189ntSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof UnicomChinaSpiderEvent) {
            // for unicomChina  联通
            Runnable runnable = () -> unicomChinaSpider
                    .onEvent((UnicomChinaSpiderEvent) event);
            executor.execute(runnable);
        } else if (event instanceof PufaBankEvent) {
            // for pufaBank  浦发
            Runnable runnable = () -> pufaBankSpider
                    .onEvent((PufaBankEvent) event);
            executor.execute(runnable);
        }else if (event instanceof PingAnSpiderEvent){
            // for pingan  平安
            Runnable runnable = () -> pinganSpider
                    .onEvent((PingAnSpiderEvent) event);
            executor.execute(runnable);
        }else if (event instanceof PingAnOilSpiderEvent){
            // for pinganOil  平安油卡
            Runnable runnable = () -> pinganOilSpider
                    .onEvent((PingAnOilSpiderEvent) event);
            executor.execute(runnable);
        }else if(event instanceof PingAnxykSpiderEvent){
            // for pinganOil  平安信用卡
            Runnable runnable = () -> pinganxykSpider
                    .onEvent((PingAnxykSpiderEvent) event);
            executor.execute(runnable);
        }else if(event instanceof BocomMileageEvent){
            // for 交通银行兑换里程
            Runnable runnable = () -> bocomMileageSpider
                    .onEvent((BocomMileageEvent) event);
            executor.execute(runnable);
        }
    }

    public void start() {
        isStopping = false;
    }

    public void stop() {
        isStopping = true;
    }

}
