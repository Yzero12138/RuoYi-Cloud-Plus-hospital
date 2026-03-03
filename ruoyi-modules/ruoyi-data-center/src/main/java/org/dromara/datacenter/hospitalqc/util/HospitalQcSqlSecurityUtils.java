package org.dromara.datacenter.hospitalqc.util;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * SQL security validator.
 */
public final class HospitalQcSqlSecurityUtils {

    private static final Pattern FORBIDDEN_SQL = Pattern.compile(
        "\\b(insert|update|delete|drop|alter|truncate|create|replace|merge|grant|revoke|call|execute|exec|commit|rollback)\\b",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern COMMENT_SQL = Pattern.compile("(--|/\\*|\\*/)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LIMIT_SQL = Pattern.compile("\\blimit\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern START_TIME_PARAM = Pattern.compile("(?i):startTime\\b");
    private static final Pattern END_TIME_PARAM = Pattern.compile("(?i):endTime\\b");

    private HospitalQcSqlSecurityUtils() {
    }

    public static void validateCountSql(String sql, String label) {
        validate(sql, label, false);
    }

    public static void validateDetailSql(String sql, String label) {
        validate(sql, label, true);
    }

    private static void validate(String sql, String label, boolean detailSql) {
        if (StringUtils.isBlank(sql)) {
            throw new ServiceException(label + "不能为空");
        }
        String normalized = sql.trim().toLowerCase(Locale.ROOT);
        if (!(normalized.startsWith("select") || normalized.startsWith("with"))) {
            throw new ServiceException(label + "仅允许 SELECT 或 WITH 语句");
        }
        if (normalized.contains(";")) {
            throw new ServiceException(label + "禁止包含分号");
        }
        if (COMMENT_SQL.matcher(normalized).find()) {
            throw new ServiceException(label + "禁止包含注释语法");
        }
        if (FORBIDDEN_SQL.matcher(normalized).find()) {
            throw new ServiceException(label + "包含高风险关键字");
        }
        if (detailSql && LIMIT_SQL.matcher(normalized).find()) {
            throw new ServiceException(label + "禁止使用 LIMIT，分页由系统控制");
        }
        if (!START_TIME_PARAM.matcher(sql).find() || !END_TIME_PARAM.matcher(sql).find()) {
            throw new ServiceException(label + "必须包含 :startTime 和 :endTime 参数");
        }
    }
}

