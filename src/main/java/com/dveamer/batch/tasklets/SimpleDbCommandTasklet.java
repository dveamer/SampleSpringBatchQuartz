package com.dveamer.batch.tasklets;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

public class SimpleDbCommandTasklet implements Tasklet {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final String sql;
    private final Map<String, ?> paramMap;

    public SimpleDbCommandTasklet(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                  String sql, Map<String, ?> paramMap) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.sql = sql;
        this.paramMap = paramMap;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        namedParameterJdbcTemplate.update(sql, paramMap);
        return RepeatStatus.FINISHED;
    }

}
