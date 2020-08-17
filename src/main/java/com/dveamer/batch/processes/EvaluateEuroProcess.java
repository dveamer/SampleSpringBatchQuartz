package com.dveamer.batch.processes;

import com.dveamer.batch.domains.exchangerate.ExchangeRate;
import com.dveamer.batch.domains.price.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;


public class EvaluateEuroProcess implements ItemProcessor<Price, Price> {

    Logger logger = LoggerFactory.getLogger(getClass());

    private final ExchangeRate exchangeRate;

    public EvaluateEuroProcess(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public Price process(Price price) {
        Price copied = price.copy();
        copied.evaluateEuroDolor(exchangeRate);
        logger.info("price : {} -> {}", price, copied);
        return copied;
    }

}
