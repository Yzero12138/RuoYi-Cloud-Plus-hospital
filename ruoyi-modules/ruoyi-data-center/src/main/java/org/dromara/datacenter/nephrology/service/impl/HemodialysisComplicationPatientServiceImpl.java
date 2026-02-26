package org.dromara.datacenter.nephrology.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.HemodialysisComplicationPatient;
import org.dromara.datacenter.nephrology.domain.bo.HemodialysisComplicationPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.HemodialysisComplicationPatientVo;
import org.dromara.datacenter.nephrology.mapper.HemodialysisComplicationPatientMapper;
import org.dromara.datacenter.nephrology.service.IHemodialysisComplicationPatientService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;

/**
 * Hemodialysis complication patient service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class HemodialysisComplicationPatientServiceImpl implements IHemodialysisComplicationPatientService {

    private final HemodialysisComplicationPatientMapper baseMapper;

    @Override
    public TableDataInfo<HemodialysisComplicationPatientVo> selectPageList(HemodialysisComplicationPatientBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HemodialysisComplicationPatient> lqw = buildQueryWrapper(bo);
        Page<HemodialysisComplicationPatientVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public HemodialysisComplicationPatientVo selectById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public List<HemodialysisComplicationPatientVo> selectList(HemodialysisComplicationPatientBo bo) {
        LambdaQueryWrapper<HemodialysisComplicationPatient> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<HemodialysisComplicationPatient> buildQueryWrapper(HemodialysisComplicationPatientBo bo) {
        LambdaQueryWrapper<HemodialysisComplicationPatient> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, HemodialysisComplicationPatient::getId, bo.getId());
        lqw.eq(StringUtils.isNotBlank(bo.getMedicalRecordNo()),
            HemodialysisComplicationPatient::getMedicalRecordNo, bo.getMedicalRecordNo());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardNo()),
            HemodialysisComplicationPatient::getIdCardNo, bo.getIdCardNo());

        DateRange range = buildDischargeTimeRange(bo.getDischargeTimeType(), bo.getDischargeTimeValue());
        lqw.between(range != null, HemodialysisComplicationPatient::getDischargeTime, range == null ? null : range.start(), range == null ? null : range.end());

        lqw.eq(HemodialysisComplicationPatient::getIsDeleted, 0);
        lqw.orderByDesc(HemodialysisComplicationPatient::getDischargeTime);
        lqw.orderByDesc(HemodialysisComplicationPatient::getId);
        return lqw;
    }

    private DateRange buildDischargeTimeRange(String type, String value) {
        if (StringUtils.isBlank(type) || StringUtils.isBlank(value)) {
            return null;
        }
        return switch (type) {
            case "year" -> parseYearRange(value);
            case "quarter" -> parseQuarterRange(value);
            case "month" -> parseMonthRange(value);
            default -> null;
        };
    }

    private DateRange parseYearRange(String value) {
        if (!value.matches("^\\d{4}$")) {
            return null;
        }
        int year = Integer.parseInt(value);
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end));
    }

    private DateRange parseQuarterRange(String value) {
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

    private DateRange parseMonthRange(String value) {
        if (!value.matches("^\\d{4}-\\d{2}$")) {
            return null;
        }
        YearMonth yearMonth = YearMonth.parse(value);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return new DateRange(toDate(start), toDate(end));
    }

    private Date toDate(LocalDateTime localDateTime) {
        return java.sql.Timestamp.valueOf(localDateTime);
    }

    private record DateRange(Date start, Date end) {
    }
}
