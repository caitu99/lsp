package com.caitu99.lsp.parser;

import com.caitu99.lsp.model.parser.ParserContext;
import com.caitu99.lsp.parser.utils.ConfigHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseConfig {

    private ParserContext context;

    private ConfigHelper configHelper = ConfigHelper.getInstance();

    private Map<String, String> configVersion = new HashMap<>();

    public BaseConfig(ParserContext context) {
        this.context = context;
    }

    public String get(Class clz, String key) {
        return configHelper.get(clz, key);
    }

    public String get(String key) {
        String version = configVersion.get(key);
        if (version != null)
            return configHelper.get(this.getClass(), key, version);

        String value = configHelper.get(this.getClass(), key);
        if(value == null)
            return null;

        version = configHelper.getLastVersion(this.getClass(), key);
        configVersion.put(key, version);

        return value;
    }

    public ParserContext getContext() {
        return context;
    }

    public void setContext(ParserContext context) {
        this.context = context;
    }

    public Map<String, String> getConfigure() {
        return configVersion;
    }

}
