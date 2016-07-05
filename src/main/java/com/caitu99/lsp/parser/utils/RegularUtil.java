/*

 * Copyright (c) 2015-2020 by caitu99
 * All rights reserved.
 */
package com.caitu99.lsp.parser.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caitu99.lsp.model.parser.bank.BankBill;

/**
 * 
 * @Description: (类职责详细描述,可空)
 * @ClassName: RegularUtil
 * @author lhj
 * @date 2015年12月16日 上午11:58:09
 * @Copyright (c) 2015-2020 by caitu99
 */
public class RegularUtil {

	private final static Logger logger = LoggerFactory
			.getLogger(RegularUtil.class);

	public static void main(String[] args) {
		// 初始化
		String encoding = "utf-8";
		String dirPath = "D:\\test\\mail";
		File dir = new File(dirPath);
		File[] array = dir.listFiles();
		List<BankBill> cardList = new ArrayList<>();
		for (int i = 0; i < array.length; i++) {
			String account = null;
			Date billMonth = null;
			try {
				File file = new File(dirPath + "\\1.html");
				if (file.isFile() && file.exists()) {
					InputStreamReader read = new InputStreamReader(
							new FileInputStream(file), encoding);// 考虑到编码格式
					BufferedReader bufferedReader = new BufferedReader(read);
					StringBuffer sb = new StringBuffer("");
					String lineTxt = null;
					while ((lineTxt = bufferedReader.readLine()) != null) {
						sb.append(lineTxt);
					}
					read.close();
					// 正则表达式定义
					String nameReg = ">.*，您好！";// 账户
					String billMonthReg = "您.*月信用卡个人卡账单已出";// 账单月
					String body = sb.toString();
					// 解析开始
					// 获取账户
					Pattern pattern = Pattern.compile(nameReg);
					Matcher matcher = pattern.matcher(body);
					if (matcher.find()) {
						String group = matcher.group();
						group = group.substring(group.lastIndexOf(">") + 1);
						if (group.indexOf("先生") != -1)
							account = group.substring(0, group.indexOf("先生"));
						else if (group.indexOf("女士") != -1)
							account = group.substring(0, group.indexOf("女士"));
					} else {
						logger.info("未能获取账单用户名");
						return;
					}
					// 获取账户账单月
					pattern = Pattern.compile(billMonthReg);
					matcher = pattern.matcher(DOMHelper.filterHtml(body));
					if (matcher.find()) {
						String group = matcher.group();
						group = group.substring(group.lastIndexOf("您") + 1,
								group.lastIndexOf("信用卡"));
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy年MM月");
						try {
							billMonth = format.parse(group);
						} catch (ParseException e) {
							logger.error("将账单月【{}】转为日期时报错", group, e);
						}
					} else {
						logger.info("未能获取账单月");
						return;
					}
					Document document = Jsoup.parse(body);
					// 获取总积分
					Element integralElement = document
							.getElementById("fixBand33");
					Long integral = Long.parseLong(integralElement
							.getElementsByTag("table").last()
							.getElementsByTag("td").get(1).text().trim());
					// 获取卡号
					Element listElement = document.getElementById("loopBand2");
					Elements elements = listElement.child(0).child(0)
							.children();
					boolean isFirst = true;
					Set<String> cardNoSet = new HashSet<>();
					for (Element element : elements) {
						if (isFirst) {
							isFirst = false;
							continue;
						}
						Elements s = element.getElementsByTag("table");
						cardNoSet.add(s.last().getElementsByTag("td").get(5)
								.text());
					}
					for (String cardNo : cardNoSet) {
						BankBill bankCard = new BankBill();
						bankCard.setName(account);
						bankCard.setBillDay(billMonth);
						bankCard.setCardNo(cardNo);
						bankCard.setIntegral(integral);
						cardList.add(bankCard);
					}
					System.out.println("----------");
				} else {
					System.out.println("找不到指定的文件");
				}
			} catch (Exception e) {
				logger.error("解析账单时出错", e);
			}
		}
	}
}
