package com.dveamer.batch.jobs;

import com.dveamer.batch.domains.exchangerate.ExchangeRate;
import com.dveamer.batch.domains.price.Price;
import com.dveamer.batch.domains.price.PriceSql;
import com.dveamer.batch.helper.BatchHelper;
import com.dveamer.batch.helper.JobExecutionStorageListener;
import com.dveamer.batch.helper.JobScopeUtil;
import com.dveamer.batch.helper.NamedParameterJdbcCursorItemReaderBuilder;
import com.dveamer.batch.processes.EvaluateEuroProcess;
import com.dveamer.batch.processes.EvaluateUsDollarProcess;
import com.dveamer.batch.tasklets.FindExchangeRateTasklet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class EvaluateForeignCurrencyJob {
    Logger logger = LoggerFactory.getLogger(EvaluateForeignCurrencyJob.class);

    private final String JOB_NAME = "EvaluateForeignCurrencyJob";
    private final String STEP_NAME = JOB_NAME + "_step";
    private final String READER_NAME = JOB_NAME + "_reader";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final NamedParameterJdbcTemplate npJdbcTemplate;

    public EvaluateForeignCurrencyJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory
            , NamedParameterJdbcTemplate npJdbcTemplate) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.npJdbcTemplate = npJdbcTemplate;
    }

    @Bean(JOB_NAME+"_trigger")
    public CronTriggerFactoryBean jobTrigger(
            @Value("${jobs.EvaluateForeignCurrencyJob.schedule}") String jobSchedule
    ) {
        return BatchHelper.cronTriggerFactoryBeanBuilder()
                .cronExpression(jobSchedule)
                .jobDetailFactoryBean(jobDetailFactoryBean())
                .build();
    }

    @Bean(JOB_NAME+"_factory")
    public JobDetailFactoryBean jobDetailFactoryBean() {
        return BatchHelper.jobDetailFactoryBeanBuilder()
                .job(job())
                .build();
    }

    @Bean(JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(stepExchangeRateEu())
                .next(stepExchangeRateUs())
                .next(stepEvaluate())
                .listener(new JobExecutionStorageListener())
                .build();
    }

    @Bean(JOB_NAME + "_step_exchangeRateEu")
    @JobScope
    public Step stepExchangeRateEu() {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet(taskletEu(0))
                .build();
    }

    @Bean(JOB_NAME + "_step_exchangeRateUs")
    @JobScope
    public Step stepExchangeRateUs() {
        return stepBuilderFactory.get(STEP_NAME)
                .tasklet(taskletUs(0))
                .build();
    }

    @Bean(JOB_NAME + "_step_evaluate")
    @JobScope
    public Step stepEvaluate() {
        return stepBuilderFactory.get(JOB_NAME + "_step_evaluate")
                .<Price, Price>chunk(BatchHelper.CHUNK_DEFAULT_SIZE)
                .reader(reader(0))
                .processor(compositeProcessor())
                .writer(writer())
                .build();
    }

    @Bean(JOB_NAME + "_taskletUs")
    @StepScope
    public Tasklet taskletUs(@Value("#{jobParameters['timestamp']}") long timestamp) {
        logger.debug("timestamp : {}", timestamp);
        return new FindExchangeRateTasklet(ExchangeRate.CurrencyType.US_DOLLAR,
                exchangeRate-> JobScopeUtil.set(ExchangeRate.CurrencyType.US_DOLLAR.name(), exchangeRate));
    }

    @Bean(JOB_NAME + "_taskletEu")
    @StepScope
    public Tasklet taskletEu(@Value("#{jobParameters['timestamp']}") long timestamp) {
        logger.debug("timestamp : {}", timestamp);
        return new FindExchangeRateTasklet(ExchangeRate.CurrencyType.EU_EURO,
                exchangeRate-> JobScopeUtil.set(ExchangeRate.CurrencyType.EU_EURO.name(), exchangeRate));
    }

    @Bean(READER_NAME)
    @StepScope
    public JdbcCursorItemReader<Price> reader(@Value("#{jobParameters['timestamp']}") long timestamp) {
        logger.debug("timestamp : {}", timestamp);
        return new NamedParameterJdbcCursorItemReaderBuilder<Price>()
                .name(READER_NAME)
                .fetchSize(BatchHelper.CUSOR_DEFAULT_SIZE_FOR_LIGHT_PROCESS)
                .namedParameterJdbcTemplate(npJdbcTemplate)
                .rowMapper(new BeanPropertyRowMapper<>(Price.class))
                .sql(PriceSql.findAll)
                .build();
    }

    @Bean(JOB_NAME + "_writer")
    public ItemWriter<Price> writer() {
        return new JdbcBatchItemWriterBuilder<Price>()
            .beanMapped()
            .namedParametersJdbcTemplate(npJdbcTemplate)
            .sql(PriceSql.updateAmount)
            .build();
    }

    @Bean(JOB_NAME + "_process")
    @StepScope
    public CompositeItemProcessor compositeProcessor() {
        List<ItemProcessor> delegates = new ArrayList<>(3);
        delegates.add(filteringOddIdProcess());
        delegates.add(evaluatingUsDollarProcess(findExchangeRate(ExchangeRate.CurrencyType.US_DOLLAR)));
        delegates.add(evaluatingEuroProcess(findExchangeRate(ExchangeRate.CurrencyType.EU_EURO)));
        CompositeItemProcessor processor = new CompositeItemProcessor<>();
        processor.setDelegates(delegates);
        return processor;
    }

    private ItemProcessor<Price, Price> evaluatingUsDollarProcess(ExchangeRate exchangeRate) {
        return new EvaluateUsDollarProcess(exchangeRate);
    }

    private ItemProcessor<Price, Price> evaluatingEuroProcess(ExchangeRate exchangeRate) {
        return new EvaluateEuroProcess(exchangeRate);
    }

    private ItemProcessor<Price, Price> filteringOddIdProcess() {
        return price -> price.getId() % 2 == 1? null: price;
    }

    private ExchangeRate findExchangeRate(ExchangeRate.CurrencyType currencyType) {
        int value = (int) JobScopeUtil.get(currencyType.name());
        return new ExchangeRate(currencyType, value);
    }

}
