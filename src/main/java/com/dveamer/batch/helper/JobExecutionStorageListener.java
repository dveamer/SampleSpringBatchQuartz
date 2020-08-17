package com.dveamer.batch.helper;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class JobExecutionStorageListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobScopeUtil.setJobExecution(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        JobScopeUtil.clear();
    }
}
