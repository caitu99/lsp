package com.caitu99.lsp.parser.utils;


import com.caitu99.lsp.model.spider.MailSrc;
import com.caitu99.lsp.parser.ITpl;
import com.caitu99.lsp.parser.ParserReactor;
import com.caitu99.lsp.parser.Template;
import com.caitu99.lsp.utils.SpringContext;
import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import com.gs.collections.impl.map.mutable.ConcurrentHashMapUnsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConfigHelper {

    private static Logger logger = LoggerFactory.getLogger(ConfigHelper.class);

    private Properties properties;

    private static ConfigHelper configure = new ConfigHelper();

    private Map<String, List<Class>> allTplMap;
    private Map<Class, String> confKeyPrefix = new HashMap<>();
    private Map<String, String> cachedLastVersion = new ConcurrentHashMap<>();
    private Map<String, String> cachedKValue = new ConcurrentHashMap<>();

    private ConfigHelper() {
        allTplMap = TplHelper.getTplClzz();
        properties = SpringContext.getBean("properties");
        initConfKeyPrefix();
    }

    public static ConfigHelper getInstance() {
        return configure;
    }

    private void initConfKeyPrefix() {
        for (Map.Entry<String, List<Class>> entry : allTplMap.entrySet()) {
            for (Class clz : entry.getValue()) {
                Template template = (Template) clz.getAnnotation(Template.class);
                String value = template.value();
                confKeyPrefix.put(clz, value);
            }
        }
    }

    public String get(Class clz, String key) {
        String prefixKey = this.confKeyPrefix.get(clz);
        prefixKey = String.format("%s.%s", prefixKey, key);

        String version = cachedLastVersion.get(prefixKey);
        if (version == null) {
            List<String> strings = getPropertyByPrefix(prefixKey);
            String last = strings.get(0); // get last version
            version = last.substring(last.lastIndexOf(".") + 1);
            cachedLastVersion.put(prefixKey, version);
        }

        return this.get(clz, key, version);
    }

    public String get(Class clz, String key, String version) {
        String fullKey = this.confKeyPrefix.get(clz);
        fullKey = String.format("%s.%s.%s", fullKey, key, version);

        String value = cachedKValue.get(fullKey);
        if(value != null)
            return value;

        value = properties.get(fullKey).toString();
        if(value == null)
            return null;

        cachedKValue.put(fullKey, value);

        return value;
    }

    public String getLastVersion(Class clz, String key) {
        String prefixKey = this.confKeyPrefix.get(clz);
        prefixKey = String.format("%s.%s", prefixKey, key);
        return cachedLastVersion.get(prefixKey);
    }

    private List<String> getPropertyByPrefix(String prefixKey) {
        List<String> strings = new ArrayList<>();
        Enumeration<?> propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String s = propertyNames.nextElement().toString();
            if (s.startsWith(prefixKey)) {
                strings.add(s);
            }
        }
        strings.sort((String s1, String s2) -> s2.compareTo(s1)); // sort by desc
        return strings;
    }

}
