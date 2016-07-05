package com.caitu99.lsp.parser;


import com.caitu99.lsp.AppConfig;
import com.caitu99.lsp.model.parser.Bill;
import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.utils.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TplTask {

    private final static Logger logger = LoggerFactory.getLogger(TplTask.class);

    private List<Class> tplClzz;
    private ParserContext context;

    public TplTask(ParserContext context, List<Class> tplClzz) {
        this.context = context;
        this.tplClzz = tplClzz;
    }

    public void process(MailSrc mailSrc) {

        if (context.getMailSrcCount().get() == 0) {
            logger.info("begin parsing: {}", mailSrc);
        }

        // parsing ...
        boolean flag = false;
        for (Class clz : tplClzz) {
            ITpl tpl;
            try {
                tpl = (ITpl) clz.newInstance();
                tpl.setContext(context);
                tpl.setMailSrc(mailSrc);

                switch (context.getStatus()) {
                    case HEAD: {
                        if (tpl.parse() && tpl.check()) {
                            flag = true;
                        }
                        break;
                    }
                    case BODY: {
                        if (tpl.is() && tpl.parse() && tpl.check()) {
                            flag = true;
                        }
                        break;
                    }
                }
                if (flag)
                    break;
            } catch (Exception e) {
                logger.error("parser error, status: {}, {} ,{}", context.getStatus(), mailSrc, e);
            }
        }

        // cannot be parsed, including error
        if (!flag) {
            context.getUnparsedMailSrcs().add(mailSrc);
            logger.error("parse failure, status: {}, {}", context.getStatus(), mailSrc);
        }

        // next status
        int curCount = context.getMailSrcCount().incrementAndGet();
        if (curCount == context.getMailSrcs().size()) {
            logger.info("complete parsing: {}", mailSrc);
            switch (context.getStatus()) {
                case HEAD: {
                    if (context.getUnparsedMailSrcs().size() > 0)
                        context.setStatus(ParserStatus.BODY);
                    else
                        context.setStatus(ParserStatus.MERGE);
                    break;
                }
                case BODY: {
                    context.setStatus(ParserStatus.MERGE);
                    break;
                }
            }

            // continue
            ParserReactor.getInstance().process(context);
        }
    }

}
