package com.dveamer.batch.helper;

import org.springframework.batch.core.JobExecution;

public class JobScopeUtil {

    private static final ThreadLocal<JobExecution> storage = new ThreadLocal<>();

    public static Object get(String key) {
        try {
            JobExecution jobExecution = storage.get();
            if(jobExecution==null) {
                return JobAsyncScopeUtil.get(key);
            }
            return jobExecution.getExecutionContext().get(key);
        }catch(Exception ex) {
            return JobAsyncScopeUtil.get(key);
        }
    }

    public static void set(String key, Object value) {
        JobExecution jobExecution = storage.get();
        jobExecution.getExecutionContext().put(key, value);
    }

    public static void setJobExecution(JobExecution jobExecution) {
        storage.set(jobExecution);
    }

    public static void clear() {
        storage.remove();
    }

}
