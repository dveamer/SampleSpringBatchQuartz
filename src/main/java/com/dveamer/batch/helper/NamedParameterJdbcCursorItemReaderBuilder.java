package com.dveamer.batch.helper;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.util.Assert;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class NamedParameterJdbcCursorItemReaderBuilder<T> {

    private JdbcCursorItemReaderBuilder<T> builder;
    private String sql;
    private Map<String,?> namedParameters;

    public NamedParameterJdbcCursorItemReaderBuilder() {
        this.builder = new JdbcCursorItemReaderBuilder<>();
    }


    public NamedParameterJdbcCursorItemReaderBuilder<T> saveState(boolean saveState) {
        this.builder.saveState(saveState);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> name(String name) {
        this.builder.name(name);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> maxItemCount(int maxItemCount) {
        this.builder.maxItemCount(maxItemCount);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> currentItemCount(int currentItemCount) {
        this.builder.currentItemCount(currentItemCount);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> namedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.builder.dataSource(namedParameterJdbcTemplate.getJdbcTemplate().getDataSource());
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> fetchSize(int fetchSize) {
        this.builder.fetchSize(fetchSize);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> maxRows(int maxRows) {
        this.builder.maxRows(maxRows);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> queryTimeout(int queryTimeout) {
        this.builder.queryTimeout(queryTimeout);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> ignoreWarnings(boolean ignoreWarnings) {
        this.builder.ignoreWarnings(ignoreWarnings);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> verifyCursorPosition(boolean verifyCursorPosition) {
        this.builder.verifyCursorPosition(verifyCursorPosition);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> driverSupportsAbsolute(boolean driverSupportsAbsolute) {
        this.builder.driverSupportsAbsolute(driverSupportsAbsolute);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> useSharedExtendedConnection(boolean useSharedExtendedConnection) {
        this.builder.useSharedExtendedConnection(useSharedExtendedConnection);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> rowMapper(RowMapper<T> rowMapper) {
        this.builder.rowMapper(rowMapper);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> beanRowMapper(Class<T> mappedClass) {
        this.builder.rowMapper(new BeanPropertyRowMapper(mappedClass));
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> connectionAutoCommit(boolean connectionAutoCommit) {
        this.builder.connectionAutoCommit(connectionAutoCommit);
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder namedParameters(Map<String,?> namedParameters) {
        this.namedParameters = namedParameters;
        return this;
    }

    public NamedParameterJdbcCursorItemReaderBuilder<T> sql(String sql) {
        this.sql = sql;
        return this;
    }

    public JdbcCursorItemReader<T> build() {

        Assert.notNull(sql, "sql is required.");

        final Object[] params = NamedParameterUtils.buildValueArray(sql, namedParameters);
        PreparedStatementSetter pss = new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                for(int i = 0; i < params.length; i++) {
                    StatementCreatorUtils.setParameterValue(ps, i + 1, -2147483648, params[i]);
                }
            }
        };

        this.builder.sql(sql);
        this.builder.preparedStatementSetter(pss);
        return this.builder.build();
    }
}
