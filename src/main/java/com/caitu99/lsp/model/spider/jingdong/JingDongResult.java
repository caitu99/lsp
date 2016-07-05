package com.caitu99.lsp.model.spider.jingdong;

public class JingDongResult {
    private String jindou;
    private String name;

    /**
     * @param jindou
     * @param name
     */
    public JingDongResult(String jindou, String name) {
        super();
        this.jindou = jindou;
        this.name = name;
    }

    public String getJindou() {
        return jindou;
    }

    public void setJindou(String jindou) {
        this.jindou = jindou;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
