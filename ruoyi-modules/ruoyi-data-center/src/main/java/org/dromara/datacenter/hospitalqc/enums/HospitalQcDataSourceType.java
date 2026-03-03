package org.dromara.datacenter.hospitalqc.enums;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.util.Locale;

/**
 * Supported external datasource types.
 */
public enum HospitalQcDataSourceType {

    MYSQL(3306),
    ORACLE(1521),
    SQLSERVER(1433);

    private final int defaultPort;

    HospitalQcDataSourceType(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public static HospitalQcDataSourceType fromType(String type) {
        if (StringUtils.isBlank(type)) {
            throw new ServiceException("Datasource type cannot be blank");
        }
        try {
            return HospitalQcDataSourceType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("Unsupported datasource type: " + type);
        }
    }

    public String buildJdbcUrl(String host, Integer port, String databaseName, String instanceName) {
        return buildJdbcUrl(host, port, databaseName, instanceName, "TLS12");
    }

    public String buildJdbcUrl(String host,
                               Integer port,
                               String databaseName,
                               String instanceName,
                               String sqlServerTlsMode) {
        if (StringUtils.isBlank(host)) {
            throw new ServiceException("Datasource host cannot be blank");
        }
        int resolvedPort = port == null || port <= 0 ? defaultPort : port;
        return switch (this) {
            case MYSQL -> buildMysqlUrl(host, resolvedPort, databaseName);
            case ORACLE -> buildOracleUrl(host, resolvedPort, databaseName, instanceName);
            case SQLSERVER -> buildSqlServerUrl(host, resolvedPort, databaseName, instanceName, sqlServerTlsMode);
        };
    }

    public String applyPageSql(String sql, int offset, int pageSize) {
        return switch (this) {
            case MYSQL -> sql + " LIMIT " + offset + ", " + pageSize;
            case ORACLE -> sql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
            case SQLSERVER -> {
                if (containsOrderBy(sql)) {
                    yield sql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
                }
                yield "SELECT * FROM (" + sql + ") t ORDER BY (SELECT 1) OFFSET " + offset
                    + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
            }
        };
    }

    public String applyLimitSql(String sql, int limit) {
        return switch (this) {
            case MYSQL -> sql + " LIMIT " + limit;
            case ORACLE -> sql + " FETCH FIRST " + limit + " ROWS ONLY";
            case SQLSERVER -> {
                if (containsOrderBy(sql)) {
                    yield sql + " OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
                }
                yield "SELECT TOP " + limit + " * FROM (" + sql + ") t";
            }
        };
    }

    private static String buildMysqlUrl(String host, int port, String databaseName) {
        if (StringUtils.isBlank(databaseName)) {
            throw new ServiceException("MYSQL requires database name");
        }
        return "jdbc:mysql://" + host + ":" + port + "/" + databaseName
            + "?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8"
            + "&useSSL=false&allowPublicKeyRetrieval=true";
    }

    private static String buildOracleUrl(String host, int port, String databaseName, String instanceName) {
        if (StringUtils.isNotBlank(instanceName)) {
            return "jdbc:oracle:thin:@" + host + ":" + port + ":" + instanceName;
        }
        if (StringUtils.isNotBlank(databaseName)) {
            return "jdbc:oracle:thin:@//" + host + ":" + port + "/" + databaseName;
        }
        throw new ServiceException("ORACLE requires database name or instance name");
    }

    private static String buildSqlServerUrl(String host,
                                            int port,
                                            String databaseName,
                                            String instanceName,
                                            String sqlServerTlsMode) {
        if (StringUtils.isBlank(databaseName)) {
            throw new ServiceException("SQLSERVER requires database name");
        }
        String tlsMode = StringUtils.isBlank(sqlServerTlsMode)
            ? "TLS12" : sqlServerTlsMode.trim().toUpperCase(Locale.ROOT);

        StringBuilder builder = new StringBuilder("jdbc:sqlserver://")
            .append(host).append(':').append(port)
            .append(";databaseName=").append(databaseName);

        switch (tlsMode) {
            case "TLS12" -> builder.append(";encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1.2");
            case "LEGACY_NO_ENCRYPT" -> builder.append(";encrypt=false");
            case "LEGACY_TLS10" -> builder.append(";encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1");
            default -> throw new ServiceException("Unsupported SQLServer TLS mode: " + sqlServerTlsMode
                + ". Available: TLS12, LEGACY_NO_ENCRYPT, LEGACY_TLS10");
        }

        if (StringUtils.isNotBlank(instanceName)) {
            builder.append(";instanceName=").append(instanceName);
        }
        return builder.toString();
    }

    private static boolean containsOrderBy(String sql) {
        if (StringUtils.isBlank(sql)) {
            return false;
        }
        return sql.toLowerCase(Locale.ROOT).contains(" order by ");
    }
}
