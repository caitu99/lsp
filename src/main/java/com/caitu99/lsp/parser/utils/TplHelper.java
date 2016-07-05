package com.caitu99.lsp.parser.utils;

import com.caitu99.lsp.model.spider.Envelope;
import com.caitu99.lsp.parser.ICard;
import com.caitu99.lsp.parser.ITpl;
import com.caitu99.lsp.parser.Template;
import com.caitu99.lsp.utils.ClassUtils;
import com.caitu99.lsp.utils.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TplHelper {

    private final static Logger logger = LoggerFactory.getLogger(TplHelper.class);

    /**
     * 获取所以的模板类
     * @return
     */
    public static Map<String, List<Class>> getTplClzz() {
        Map<String, List<Class>> bankParsersMap = new HashMap<>();
        try {
            List<Class> bankParserClasses = ClassUtils.getAllClassByInterface(ITpl.class);
            for (Class clz : bankParserClasses) {
                if (!Modifier.isAbstract(clz.getModifiers())) { // not abstract

                    /*Field field = ClassUtils.getField(clz, "card", true);
                    Class cardClz = null;
                    if (field == null) {
                        logger.warn("can not get field by name: card");
                        List<Class> cardClzz = ClassUtils.getAllClassInPackageByInterface(clz.getPackage(), ICard.class);
                        if (cardClzz.size() == 0) {
                            logger.error("can not get card by interface");
                        } else if (cardClzz.size() > 1) {
                            logger.error("get card more than one");
                        }
                        cardClz = cardClzz.get(0);
                    } else {
                        cardClz = field.getType();
                    }*/

                    ITpl tpl = (ITpl) clz.newInstance();
                    ICard iCard = tpl.getCard();
                    if (iCard == null)
                        logger.error("can not find bean by class {}", clz.getName());

                    String card = iCard.getName();
                    if (bankParsersMap.get(card) == null) {
                        bankParsersMap.put(card, new ArrayList<>());
                    }
                    bankParsersMap.get(card).add(clz);
                }
            }
        } catch (Exception e) {
            logger.error("get clazz error", e);
        }
        return bankParsersMap;
    }

    /**
     * 获取卡片类
     * @return
     */
    public static Map<String, Class> getCardClzz() {
        Map<String, Class> bankParsersMap = new HashMap<>();
        try {
            List<Class> bankParserClasses = ClassUtils.getAllClassByInterface(ICard.class);
            for (Class clz : bankParserClasses) {
                if (!Modifier.isAbstract(clz.getModifiers())) {
                    ICard iCard = (ICard) SpringContext.getBean(clz);
                    if (iCard == null)
                        logger.error("can not find bean by class {}", clz.getName());
                    String card = iCard.getName();
                    bankParsersMap.put(card, clz);
                }
            }
        } catch (Exception e) {
            logger.error("get clazz error", e);
        }
        return bankParsersMap;
    }

    /**
     * 获取模板名称
     * @param clz
     * @return
     */
    public static String getTplName(Class clz) {
        Template template = (Template) clz.getAnnotation(Template.class);
        return template.value();
    }

    /**
     * 获取卡片实例
     * @return
     */
    public static Map<String, ICard> getAllCard() {
        Map<String, ICard> cards = new HashMap<>();
        Map<String, Class> cardClazz = getCardClzz();
        for (Map.Entry<String, Class> entry : cardClazz.entrySet()) {
            ICard card = (ICard) SpringContext.getBean(entry.getValue());
            cards.put(card.getName(), card);
        }
        return cards;
    }


    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Map<String, Class> cardClazz = getCardClzz();
        System.out.println();
    }

}
