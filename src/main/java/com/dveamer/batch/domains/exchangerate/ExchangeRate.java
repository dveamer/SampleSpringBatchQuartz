package com.dveamer.batch.domains.exchangerate;

public class ExchangeRate {

    public enum CurrencyType {
        KR_WON,
        US_DOLLAR,
        EU_EURO
    }

    private final CurrencyType currencyType;
    private final double value;

    public ExchangeRate(CurrencyType currencyType, double value) {
        this.currencyType = currencyType;
        this.value = value;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public double getValue() {
        return value;
    }

}
