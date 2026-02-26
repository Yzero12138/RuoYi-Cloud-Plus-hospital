package org.dromara.datacenter.nephrology.util;

import org.dromara.common.core.utils.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;

/**
 * Parse a year/quarter/month value to a datetime range.
 */
public final class NephrologyTimeRangeUtils {

    private NephrologyTimeRangeUtils() {
    }

    public static DateRange parse(String type, String value) {
        if (StringUtils.isBlank(type) || StringUtils.isBlank(value)) {
            return null;
        }
        return switch (type) {
            case "year" -> parseYear(value);
            case "quarter" -> parseQuarter(value);
            case "month" -> parseMonth(value);
            default -> null;
        };
    }

    private static DateRange parseYear(String value) {
        if (!value.matches("^\\d{4}$")) {
            return null;
        }
        int year = Integer.parseInt(value);
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end));
    }

    private static DateRange parseQuarter(String value) {
        if (!value.matches("^\\d{4}-Q[1-4]$")) {
            return null;
        }
        String[] split = value.split("-Q");
        int year = Integer.parseInt(split[0]);
        int quarter = Integer.parseInt(split[1]);
        int startMonth = (quarter - 1) * 3 + 1;
        YearMonth endMonth = YearMonth.of(year, startMonth + 2);
        LocalDateTime start = LocalDate.of(year, startMonth, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, endMonth.getMonthValue(), endMonth.lengthOfMonth()).atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end));
    }

    private static DateRange parseMonth(String value) {
        if (!value.matches("^\\d{4}-\\d{2}$")) {
            return null;
        }
        YearMonth yearMonth = YearMonth.parse(value);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end));
    }

    private static Date toDate(LocalDateTime localDateTime) {
        return java.sql.Timestamp.valueOf(localDateTime);
    }

    public record DateRange(Date start, Date end) {
    }
}
