package com.dveamer.batch.domains.price;

import com.dveamer.batch.domains.exchangerate.ExchangeRate;

public class Price {
    private long id;
    private long amount;
    private long amountUs;
    private long amountEu;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getAmountUs() {
        return amountUs;
    }

    public void setAmountUs(long amountUs) {
        this.amountUs = amountUs;
    }

    public long getAmountEu() {
        return amountEu;
    }

    public void setAmountEu(long amountEu) {
        this.amountEu = amountEu;
    }

    public Price copy() {
        Price copied = new Price();
        copied.setId(this.id);
        copied.setAmount(this.amount);
        copied.setAmountUs(this.amountUs);
        copied.setAmountEu(this.amountEu);
        return copied;
    }

    public void evaluateUsDolor(ExchangeRate exchangeRate) {
        long amountUs = Math.round(amount / exchangeRate.getValue());
        this.setAmountUs(amountUs);
    }

    public void evaluateEuroDolor(ExchangeRate exchangeRate) {
        long amountEu = Math.round(amount / exchangeRate.getValue());
        this.setAmountEu(amountEu);
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", amount=" + amount +
                ", amountUs=" + amountUs +
                ", amountEu=" + amountEu +
                '}';
    }
}
