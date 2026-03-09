package org.dromara.datacenter.hospitalqc.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.datacenter.hospitalqc.config.HospitalQcProperties;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcDataSource;
import org.dromara.datacenter.hospitalqc.enums.HospitalQcDataSourceType;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JDBC executor for dynamic external datasources.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class HospitalQcJdbcExecutor {

    private static final AtomicBoolean LEGACY_TLS10_APPLIED = new AtomicBoolean(false);

    private final HospitalQcProperties properties;

    public <T> T executeInSession(HospitalQcDataSource dataSource,
                                  String plainPassword,
                                  int timeoutSeconds,
                                  Function<JdbcSession, T> action) {
        if (dataSource == null) {
            throw new ServiceException("数据源不存在");
        }
        HospitalQcDataSourceType sourceType = HospitalQcDataSourceType.fromType(dataSource.getSourceType());
        prepareLegacySqlServerTlsIfRequired(sourceType);
        String jdbcUrl = sourceType.buildJdbcUrl(
            dataSource.getHost(),
            dataSource.getPort(),
            dataSource.getDatabaseName(),
            dataSource.getInstanceName(),
            properties.getSqlserverTlsMode());

        try {
            DriverManager.setLoginTimeout(Math.max(timeoutSeconds, 1));
            Connection connection = DriverManager.getConnection(jdbcUrl, dataSource.getUsername(), plainPassword);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbcTemplate.setQueryTimeout(Math.max(timeoutSeconds, 1));
            NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            try {
                return action.apply(new JdbcSession(namedTemplate, sourceType));
            } finally {
                connection.close();
            }
        } catch (Exception ex) {
            log.error("Execute dynamic datasource session failed", ex);
            throw new ServiceException("连接数据源失败: " + rootMessage(ex));
        }
    }

    public long queryForCount(JdbcSession session, String sql, Map<String, Object> params) {
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") _cnt";
        try {
            Long count = session.template().queryForObject(countSql, params, Long.class);
            return count == null ? 0L : count;
        } catch (EmptyResultDataAccessException ex) {
            return 0L;
        } catch (DataAccessException ex) {
            throw new ServiceException("SQL执行失败: " + rootMessage(ex));
        }
    }

    /**
     * Execute a SQL that returns a single numeric value (scalar).
     * Unlike {@link #queryForCount} which wraps the SQL in COUNT(*),
     * this method returns the actual value from the first column of the first row.
     */
    public long queryForScalar(JdbcSession session, String sql, Map<String, Object> params) {
        try {
            Long value = session.template().queryForObject(sql, params, Long.class);
            return value == null ? 0L : value;
        } catch (EmptyResultDataAccessException ex) {
            return 0L;
        } catch (DataAccessException ex) {
            throw new ServiceException("SQL执行失败: " + rootMessage(ex));
        }
    }

    public List<Map<String, Object>> queryForRows(JdbcSession session,
                                                   String sql,
                                                   Map<String, Object> params,
                                                   int limit) {
        String limitedSql = session.sourceType().applyLimitSql(sql, limit);
        try {
            return session.template().query(limitedSql, params, dynamicRowMapper());
        } catch (DataAccessException ex) {
            throw new ServiceException("SQL执行失败: " + rootMessage(ex));
        }
    }

    /**
     * Extract column labels from the SQL result metadata.
     * Returns column names even when the result set has 0 rows.
     */
    public List<String> queryForColumnNames(JdbcSession session, String sql, Map<String, Object> params) {
        String limitedSql = session.sourceType().applyLimitSql(sql, 0);
        try {
            List<String> result = session.template().query(limitedSql, params, rs -> {
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                java.util.ArrayList<String> cols = new java.util.ArrayList<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    cols.add(meta.getColumnLabel(i));
                }
                return cols;
            });
            return result == null ? List.of() : result;
        } catch (DataAccessException ex) {
            throw new ServiceException("SQL执行失败: " + rootMessage(ex));
        }
    }

    public List<Map<String, Object>> queryForPage(JdbcSession session,
                                                   String sql,
                                                   Map<String, Object> params,
                                                   int offset,
                                                   int pageSize) {
        String pageSql = session.sourceType().applyPageSql(sql, offset, pageSize);
        try {
            return session.template().query(pageSql, params, dynamicRowMapper());
        } catch (DataAccessException ex) {
            throw new ServiceException("SQL执行失败: " + rootMessage(ex));
        }
    }

    private static RowMapper<Map<String, Object>> dynamicRowMapper() {
        return (resultSet, rowNum) -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columns = metaData.getColumnCount();
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columns; i++) {
                row.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            return row;
        };
    }

    private String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        String message = cursor.getMessage();
        return StringUtils.isBlank(message) ? cursor.toString() : message;
    }

    private void prepareLegacySqlServerTlsIfRequired(HospitalQcDataSourceType sourceType) {
        if (sourceType != HospitalQcDataSourceType.SQLSERVER) {
            return;
        }
        String tlsMode = StringUtils.isBlank(properties.getSqlserverTlsMode())
            ? "TLS12" : properties.getSqlserverTlsMode().trim().toUpperCase(Locale.ROOT);
        if (!"LEGACY_TLS10".equals(tlsMode)) {
            return;
        }
        if (!LEGACY_TLS10_APPLIED.compareAndSet(false, true)) {
            return;
        }
        try {
            System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            Security.setProperty("jdk.tls.legacyAlgorithms", "TLSv1,TLSv1.1,3DES_EDE_CBC");
            String disabledAlgorithms = Security.getProperty("jdk.tls.disabledAlgorithms");
            if (StringUtils.isNotBlank(disabledAlgorithms)) {
                String adjusted = Arrays.stream(disabledAlgorithms.split(","))
                    .map(String::trim)
                    .filter(item -> !item.equalsIgnoreCase("TLSv1")
                        && !item.equalsIgnoreCase("TLSv1.1")
                        && !item.equalsIgnoreCase("3DES_EDE_CBC"))
                    .collect(Collectors.joining(", "));
                Security.setProperty("jdk.tls.disabledAlgorithms", adjusted);
            }
            log.warn("Unsafe legacy TLS settings applied for SQLServer LEGACY_TLS10 mode");
        } catch (Exception ex) {
            LEGACY_TLS10_APPLIED.set(false);
            log.warn("Apply legacy TLS settings failed", ex);
        }
    }

    public record JdbcSession(NamedParameterJdbcTemplate template, HospitalQcDataSourceType sourceType) {
    }
}
