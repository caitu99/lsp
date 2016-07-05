package com.caitu99.lsp.mongodb.parser;

import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.mongodb.model.MGLog;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class MGBillServiceTest {

    private final static Logger logger = LoggerFactory.getLogger(MGBillServiceTest.class);

    @Autowired
    private MGBillService mgBillService;

    @Autowired
    private MGLogService mgLogService;

    private MGBill mgBill = new MGBill();
    private MGLog mgLog = new MGLog();

    @Before
    public void init() {
        mgBill.setUserId(1);
        mgBill.setCard("中国");
        mgBill.setName("bobo");
        mgBill.setCardNo("1234");
        mgBill.setDate(new Date());
    }

    @Test
    public void testInsert() throws Exception {
        List<MGLog> mgLogs = new ArrayList<>();
        for (int i = 0; i < 100000; ++i) {
            mgLog.setbId("1111111");
            mgLog.setSrcId("111111");
            mgLog.setCreated(new Date());
            mgLog.setContextId("111111");
            mgLogs.add(mgLog);
        }
        logger.info("begin");
        mgLogService.batchInsert(mgLogs);
        logger.info("end");
    }

    @Test
    public void testGet() throws Exception {
        List<MGBill> bills = mgBillService.get(mgBill);
        logger.info(String.valueOf(bills.size()));
        MGBill mgBill = mgBillService.getLast(this.mgBill);
        logger.info(mgBill.toString());
    }
}