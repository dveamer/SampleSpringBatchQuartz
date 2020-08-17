package com.dveamer.batch.helper;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

/**
 * Quartz Schedule 에 등록된 Job 을 Spring Batch Job 으로 실행시키기 위한 Executor class.
 */
@DisallowConcurrentExecution
public class BatchJobExecutor implements org.quartz.Job {

    Logger logger = LoggerFactory.getLogger(getClass());
    
    private final JobLocator jobLocator;
    private final JobLauncher jobLauncher;

    public BatchJobExecutor(JobLocator jobLocator, JobLauncher jobLauncher) {
        this.jobLocator = jobLocator;
        this.jobLauncher = jobLauncher;
    }

    /**
     * Quartz Job 으로 들어온 Parameter 를 Spring Batch Parameter 로 변환하여 Spring Batch Job 실행
     * 
     * @param context quartz execution context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String jobName = BatchHelper.getJobName(context.getMergedJobDataMap());
            logger.info("[{}] started.", jobName);
            JobParameters jobParameters = BatchHelper.getJobParameters(context);
            jobLauncher.run(jobLocator.getJob(jobName), jobParameters);
            logger.info("[{}] completed.", jobName);
        } catch (NoSuchJobException | JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException | SchedulerException e) {
            logger.error("job execution exception! - {}", e.getMessage(), e);
            throw new JobExecutionException();
        }
    }
}
