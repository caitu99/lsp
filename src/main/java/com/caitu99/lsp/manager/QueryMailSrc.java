package com.caitu99.lsp.manager;

import com.caitu99.lsp.mongodb.model.MGBill;
import com.caitu99.lsp.mongodb.model.MGLog;
import com.caitu99.lsp.mongodb.parser.MGBillService;
import com.caitu99.lsp.mongodb.parser.MGLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lion on 2015/12/29 0029.
 */
@Controller
public class QueryMailSrc {
    private final static Logger logger = LoggerFactory.getLogger(QueryMailSrc.class);

    @Autowired
    private MGLogService mgLogService;

    @Autowired
    private MGBillService mgBillService;

    @RequestMapping("/lsp/query/mail/")
    public String getMailSrcByMailSrcId(HttpServletRequest request,HttpServletResponse response,Model model) {
        String uuid = request.getParameter("uuid");
        Map<String,Object> map = new HashMap<>();
        if (uuid != null && uuid.length() != 0) {
            model.addAttribute("uuid", uuid);
            try {
                MGLog mgLog = mgLogService.getMGLogByMailSrcId(uuid);
                if (mgLog != null) {
                    MGBill mgBill = mgBillService.get(mgLog.getbId());
                    if (mgBill != null) {
                        model.addAttribute("id", mgBill.getId());
                        model.addAttribute("userid", mgBill.getUserId());
                        model.addAttribute("name", mgBill.getName());
                        model.addAttribute("bank", mgBill.getBank());
                        model.addAttribute("card", mgBill.getCard());
                        model.addAttribute("integral", mgBill.getIntegral());
                        model.addAttribute("date", mgBill.getDate());
                        model.addAttribute("account", mgBill.getAccount());
                        model.addAttribute("title", mgBill.getTitle());
                        model.addAttribute("body", mgBill.getBody());
                        model.addAttribute("sDate", mgBill.getsDate());
                        model.addAttribute("tpl", mgBill.getTpl());
                        model.addAttribute("status", mgBill.getStatus());
                        Map map1 = mgBill.getConfig();
                        if (map1 != null) {
                            model.addAttribute("config.name", mgBill.getConfig().getOrDefault("name", "0"));
                        }
                        else {
                            model.addAttribute("config.name", null);
                        }
                        model.addAttribute("message", "查询成功");
                        model.addAttribute("displaydata", "1");
                    }
                    else {
                        model.addAttribute("message", "未获取到MGBill");
                    }
                } else {
                    model.addAttribute("message", "未查询到MGLog");
                }
            } catch (Exception e) {
                model.addAttribute("message", "程序出错");
                logger.error("",e);
            }
            model.addAttribute("display", "1");
        }

        return "mail/msg";
    }
}
