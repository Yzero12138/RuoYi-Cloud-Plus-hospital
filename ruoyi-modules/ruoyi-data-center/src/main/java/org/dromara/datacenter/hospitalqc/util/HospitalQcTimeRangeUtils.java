package org.dromara.datacenter.hospitalqc.util;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.datacenter.hospitalqc.constants.HospitalQcConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Time range parser utilities.
 */
public final class HospitalQcTimeRangeUtils {

    private HospitalQcTimeRangeUtils() {
    }

    public static DateRange parseReportRange(String timeType, String timeValue, Date startTime, Date endTime) {
        if (HospitalQcConstants.TIME_TYPE_CUSTOM.equals(timeType)) {
            if (startTime == null || endTime == null) {
                throw new ServiceException("自定义时间必须填写开始和结束时间");
            }
            if (startTime.after(endTime)) {
                throw new ServiceException("开始时间不能晚于结束时间");
            }
            return new DateRange(startTime, endTime, "自定义");
        }
        if (HospitalQcConstants.TIME_TYPE_YEAR.equals(timeType)) {
            return parseYearRange(timeValue);
        }
        if (HospitalQcConstants.TIME_TYPE_MONTH.equals(timeType)) {
            return parseMonthRange(timeValue);
        }
        return parseQuarterRange(timeValue);
    }

    public static DateRange parseYearRange(String value) {
        if (StringUtils.isBlank(value) || !value.matches("^\\d{4}$")) {
            throw new ServiceException("年份格式错误，示例：2026");
        }
        int year = Integer.parseInt(value);
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end), value);
    }

    public static DateRange previousRange(DateRange range) {
        LocalDateTime start = toLocalDateTime(range.startTime());
        LocalDateTime end = toLocalDateTime(range.endTime());
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        LocalDateTime prevEnd = start.minusDays(1).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime prevStart = prevEnd.minusDays(Math.max(days - 1, 0)).withHour(0).withMinute(0).withSecond(0);
        return new DateRange(toDate(prevStart), toDate(prevEnd), "上一周期");
    }

    public static List<DateRange> buildQuarterRanges(int year) {
        List<DateRange> ranges = new ArrayList<>();
        for (int quarter = 1; quarter <= 4; quarter++) {
            ranges.add(parseQuarterRange(year + "-Q" + quarter));
        }
        return ranges;
    }

    /**
     * Build quarter ranges for dashboard trend chart.
     * Returns a sliding window of recent quarters ending at the current quarter.
     *
     * @param windowSize number of quarters to include (min 1)
     * @return list of DateRange for each quarter in the window
     */
    public static List<DateRange> buildQuarterWindow(int windowSize) {
        if (windowSize < 1) {
            windowSize = 1;
        }
        if (windowSize > 12) {
            windowSize = 12; // Max 12 quarters (3 years)
        }

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        int currentQuarter = (currentMonth - 1) / 3 + 1;

        List<DateRange> ranges = new ArrayList<>();

        // Calculate starting point
        int totalQuartersBack = windowSize - 1;
        int startYear = currentYear;
        int startQuarter = currentQuarter;

        // Move backwards to find start quarter
        for (int i = 0; i < totalQuartersBack; i++) {
            startQuarter--;
            if (startQuarter < 1) {
                startQuarter = 4;
                startYear--;
            }
        }

        // Build ranges from start to current
        int year = startYear;
        int quarter = startQuarter;
        for (int i = 0; i < windowSize; i++) {
            ranges.add(parseQuarterRange(year + "-Q" + quarter));
            quarter++;
            if (quarter > 4) {
                quarter = 1;
                year++;
            }
        }

        return ranges;
    }

    public static DateRange parseQuarterRange(String value) {
        if (StringUtils.isBlank(value) || !value.matches("^\\d{4}-Q[1-4]$")) {
            throw new ServiceException("季度格式错误，示例：2026-Q1");
        }
        String[] split = value.split("-Q");
        int year = Integer.parseInt(split[0]);
        int quarter = Integer.parseInt(split[1]);
        int startMonth = (quarter - 1) * 3 + 1;
        YearMonth endMonth = YearMonth.of(year, startMonth + 2);
        LocalDateTime start = LocalDate.of(year, startMonth, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, endMonth.getMonthValue(), endMonth.lengthOfMonth()).atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end), value);
    }

    public static DateRange parseMonthRange(String value) {
        if (StringUtils.isBlank(value) || !value.matches("^\\d{4}-\\d{2}$")) {
            throw new ServiceException("月份格式错误，示例：2026-01");
        }
        YearMonth yearMonth = YearMonth.parse(value);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end), value);
    }

    public static int resolveYear(Integer year) {
        if (year != null) {
            return year;
        }
        return Year.now().getValue();
    }

    private static Date toDate(LocalDateTime localDateTime) {
        return java.sql.Timestamp.valueOf(localDateTime);
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }

    public record DateRange(Date startTime, Date endTime, String label) {
    }
}
