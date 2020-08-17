package com.dveamer.batch.tasklets;

import com.dveamer.batch.domains.exchangerate.ExchangeRate;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.function.Consumer;

public class FindExchangeRateTasklet implements Tasklet {

    private final ExchangeRate.CurrencyType currencyType;
    private final Consumer consumer;

    public FindExchangeRateTasklet(ExchangeRate.CurrencyType currencyType, Consumer consumer) {
        this.currencyType = currencyType;
        this.consumer = consumer;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        consumer.accept(findExchangeRate(currencyType));
        return RepeatStatus.FINISHED;
    }

    private int findExchangeRate(ExchangeRate.CurrencyType currencyType) {
        if(currencyType == ExchangeRate.CurrencyType.EU_EURO) {
            return 1400;
        }
        return 1200;
    }
}
