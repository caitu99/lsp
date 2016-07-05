package com.caitu99.lsp.model.parser;


public enum BillType {
    INTEGRAL_INDEPENDENT(0),
    INTEGRAL_UNIFICATION(1);

    private int typeIdx;

    private BillType(int typeIdx) {
        this.typeIdx = typeIdx;
    }

    public static BillType valueOf(int typeIdx) {
        switch (typeIdx) {
            case 0: return INTEGRAL_INDEPENDENT;
            case 1: return INTEGRAL_UNIFICATION;
            default: return null;
        }
    }

    public int getTypeIdx() {
        return typeIdx;
    }

    public void setTypeIdx(int typeIdx) {
        this.typeIdx = typeIdx;
    }
}
