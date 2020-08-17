package com.dveamer.batch.jobs;

import com.dveamer.batch.domains.price.PriceSql;
import com.dveamer.batch.helper.BatchHelper;
import com.dveamer.batch.helper.JobExecutionStorageListener;
import com.dveamer.batch.tasklets.SimpleDbCommandTasklet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
public class CleanForeignCurrencyJob {
    Logger logger = LoggerFactory.getLogger(CleanForeignCurrencyJob.class);

    private final String JOB_NAME = "CleanForeignCurrencyJob";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final NamedParameterJdbcTemplate npJdbcTemplate;

    public CleanForeignCurrencyJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, NamedParameterJdbcTemplate npJdbcTemplate) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.npJdbcTemplate = npJdbcTemplate;
    }

    @Bean(JOB_NAME+"_trigger")
    public CronTriggerFactoryBean jobTrigger(
            @Value("${jobs.CleanForeignCurrencyJob.schedule}") String jobSchedule
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
                .start(step())
                .listener(new JobExecutionStorageListener())
                .build();
    }

    @Bean(JOB_NAME + "_step")
    @JobScope
    public Step step() {
        return stepBuilderFactory.get(JOB_NAME + "_step")
                .tasklet(tasklet(0))
                .build();
    }
    
    @Bean(JOB_NAME + "_tasklet")
    @StepScope
    public Tasklet tasklet(@Value("#{jobParameters['timestamp']}") long timestamp) {
        logger.debug("timestamp : {}", timestamp);
        return new SimpleDbCommandTasklet(npJdbcTemplate, PriceSql.clearForeignCurrency, null);
    }

}
