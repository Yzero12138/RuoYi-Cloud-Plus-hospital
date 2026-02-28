package org.dromara.datacenter.nephrology.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.constants.NephrologyLedgerConstants;
import org.dromara.datacenter.nephrology.domain.HdPatient;
import org.dromara.datacenter.nephrology.domain.HemodialysisComplicationPatient;
import org.dromara.datacenter.nephrology.domain.NephrologyLedgerItem;
import org.dromara.datacenter.nephrology.domain.NephrologyLedgerQuery;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerCountBo;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerDetailBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerCountVo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerDetailVo;
import org.dromara.datacenter.nephrology.mapper.HdPatientMapper;
import org.dromara.datacenter.nephrology.mapper.HemodialysisComplicationPatientMapper;
import org.dromara.datacenter.nephrology.mapper.NephrologyLedgerItemMapper;
import org.dromara.datacenter.nephrology.mapper.NephrologyLedgerQueryMapper;
import org.dromara.datacenter.nephrology.service.INephrologyLedgerService;
import org.dromara.datacenter.nephrology.util.NephrologyTimeRangeUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.time.Year;
import java.util.stream.Collectors;

/**
 * Ledger count/detail query service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
@Slf4j
public class NephrologyLedgerServiceImpl implements INephrologyLedgerService {

    private final NephrologyLedgerItemMapper ledgerItemMapper;
    private final NephrologyLedgerQueryMapper ledgerQueryMapper;
    private final HemodialysisComplicationPatientMapper complicationPatientMapper;
    private final HdPatientMapper hdPatientMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<NephrologyLedgerCountVo> selectCountTree(NephrologyLedgerCountBo bo) {
        List<NephrologyLedgerItem> ledgerItems = ledgerItemMapper.selectList(Wrappers.<NephrologyLedgerItem>lambdaQuery()
            .eq(NephrologyLedgerItem::getIsDeleted, 0)
            .eq(NephrologyLedgerItem::getStatus, 0)
            .orderByAsc(NephrologyLedgerItem::getSortOrder)
            .orderByAsc(NephrologyLedgerItem::getId));
        if (ledgerItems.isEmpty()) {
            return List.of();
        }

        NephrologyTimeRangeUtils.DateRange range = resolveRange(bo.getDischargeTimeType(), bo.getDischargeTimeValue());
        Map<String, NephrologyLedgerQuery> queryMap = loadQueryConfigs(ledgerItems);
        Map<String, Long> countCache = new HashMap<>();

        Map<Long, NephrologyLedgerCountVo> nodeMap = new HashMap<>();
        for (NephrologyLedgerItem item : ledgerItems) {
            NephrologyLedgerCountVo vo = new NephrologyLedgerCountVo();
            vo.setId(item.getId());
            vo.setParentId(item.getParentId());
            vo.setLedgerCode(item.getLedgerCode());
            vo.setLedgerName(item.getLedgerName());
            vo.setNodeType(item.getNodeType());
            vo.setQueryTarget(item.getQueryTarget());
            vo.setSortOrder(item.getSortOrder());
            vo.setCountValue(String.valueOf(calculateLeafCount(item, range, queryMap, countCache)));
            vo.setQueryable(isQueryable(item, queryMap));
            vo.setChildren(new ArrayList<>());
            nodeMap.put(vo.getId(), vo);
        }

        List<NephrologyLedgerCountVo> roots = new ArrayList<>();
        for (NephrologyLedgerCountVo vo : nodeMap.values()) {
            Long parentId = Objects.requireNonNullElse(vo.getParentId(), 0L);
            NephrologyLedgerCountVo parent = nodeMap.get(parentId);
            if (parent == null || parentId == 0L) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }

        Comparator<NephrologyLedgerCountVo> comparator = Comparator
            .comparing((NephrologyLedgerCountVo it) -> Objects.requireNonNullElse(it.getSortOrder(), 0))
            .thenComparing(NephrologyLedgerCountVo::getId);
        sortTree(roots, comparator);

        for (NephrologyLedgerCountVo root : roots) {
            aggregateCount(root);
        }

        return roots;
    }

    @Override
    public TableDataInfo<NephrologyLedgerDetailVo> selectDetailPage(NephrologyLedgerDetailBo bo, PageQuery pageQuery) {
        NephrologyLedgerItem ledgerItem = ledgerItemMapper.selectOne(Wrappers.<NephrologyLedgerItem>lambdaQuery()
            .eq(NephrologyLedgerItem::getLedgerCode, bo.getLedgerCode())
            .eq(NephrologyLedgerItem::getIsDeleted, 0)
            .eq(NephrologyLedgerItem::getStatus, 0)
            .last("limit 1"));
        if (ledgerItem == null) {
            return new TableDataInfo<>(List.of(), 0);
        }

        NephrologyTimeRangeUtils.DateRange range = resolveRange(bo.getDischargeTimeType(), bo.getDischargeTimeValue());
        NephrologyLedgerQuery queryConfig = getActiveQueryByCode(ledgerItem.getQueryTarget());
        if (queryConfig != null) {
            return queryDetailBySql(queryConfig, bo, pageQuery, range);
        }

        return switch (StringUtils.defaultString(ledgerItem.getQueryTarget())) {
            case NephrologyLedgerConstants.QUERY_TARGET_HD_COMPLICATION_NUMERATOR -> queryComplicationDetails(bo, pageQuery, range);
            case NephrologyLedgerConstants.QUERY_TARGET_HD_DISCHARGE_DENOMINATOR -> queryHdDischargeDetails(bo, pageQuery, range);
            default -> new TableDataInfo<>(List.of(), 0);
        };
    }

    private boolean isQueryable(NephrologyLedgerItem item, Map<String, NephrologyLedgerQuery> queryMap) {
        return !NephrologyLedgerConstants.QUERY_TARGET_NONE.equals(item.getQueryTarget())
            && !NephrologyLedgerConstants.NODE_TYPE_INDICATOR.equals(item.getNodeType())
            && (queryMap.containsKey(item.getQueryTarget()) || isLegacyQueryTarget(item.getQueryTarget()));
    }

    private long calculateLeafCount(NephrologyLedgerItem item,
                                    NephrologyTimeRangeUtils.DateRange range,
                                    Map<String, NephrologyLedgerQuery> queryMap,
                                    Map<String, Long> countCache) {
        String queryTarget = StringUtils.defaultString(item.getQueryTarget());
        if (NephrologyLedgerConstants.QUERY_TARGET_NONE.equals(queryTarget)) {
            return 0L;
        }
        NephrologyLedgerQuery queryConfig = queryMap.get(queryTarget);
        if (queryConfig != null) {
            return countCache.computeIfAbsent(queryTarget, key -> executeCountSql(queryConfig.getCountSql(), range));
        }
        return calculateLegacyCount(queryTarget, range);
    }

    private long calculateLegacyCount(String queryTarget, NephrologyTimeRangeUtils.DateRange range) {
        return switch (queryTarget) {
            case NephrologyLedgerConstants.QUERY_TARGET_HD_COMPLICATION_NUMERATOR -> countComplication(range);
            case NephrologyLedgerConstants.QUERY_TARGET_HD_DISCHARGE_DENOMINATOR -> countHdDischarge(range);
            default -> 0L;
        };
    }

    private boolean isLegacyQueryTarget(String queryTarget) {
        return NephrologyLedgerConstants.QUERY_TARGET_HD_COMPLICATION_NUMERATOR.equals(queryTarget)
            || NephrologyLedgerConstants.QUERY_TARGET_HD_DISCHARGE_DENOMINATOR.equals(queryTarget);
    }

    private Map<String, NephrologyLedgerQuery> loadQueryConfigs(List<NephrologyLedgerItem> ledgerItems) {
        if (ledgerItems == null || ledgerItems.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> queryCodes = ledgerItems.stream()
            .map(NephrologyLedgerItem::getQueryTarget)
            .filter(StringUtils::isNotBlank)
            .filter(code -> !NephrologyLedgerConstants.QUERY_TARGET_NONE.equals(code))
            .collect(Collectors.toSet());
        if (queryCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<NephrologyLedgerQuery> queries = ledgerQueryMapper.selectList(Wrappers.<NephrologyLedgerQuery>lambdaQuery()
            .in(NephrologyLedgerQuery::getQueryCode, queryCodes)
            .eq(NephrologyLedgerQuery::getIsDeleted, 0)
            .eq(NephrologyLedgerQuery::getStatus, 0));
        if (queries == null || queries.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, NephrologyLedgerQuery> map = new HashMap<>();
        for (NephrologyLedgerQuery query : queries) {
            map.put(query.getQueryCode(), query);
        }
        return map;
    }

    private NephrologyLedgerQuery getActiveQueryByCode(String queryCode) {
        if (StringUtils.isBlank(queryCode) || NephrologyLedgerConstants.QUERY_TARGET_NONE.equals(queryCode)) {
            return null;
        }
        return ledgerQueryMapper.selectOne(Wrappers.<NephrologyLedgerQuery>lambdaQuery()
            .eq(NephrologyLedgerQuery::getQueryCode, queryCode)
            .eq(NephrologyLedgerQuery::getIsDeleted, 0)
            .eq(NephrologyLedgerQuery::getStatus, 0)
            .last("limit 1"));
    }

    private long executeCountSql(String sql, NephrologyTimeRangeUtils.DateRange range) {
        Map<String, Object> params = buildQueryParams(range, null);
        return executeCountSql(sql, params);
    }

    private long executeCountSql(String sql, Map<String, Object> params) {
        if (StringUtils.isBlank(sql)) {
            return 0L;
        }
        try {
            Long count = namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
            return count == null ? 0L : count;
        } catch (EmptyResultDataAccessException ex) {
            return 0L;
        } catch (DataAccessException ex) {
            // Query SQL is user-configurable; fail closed and treat as 0.
            // The exception is logged so misconfigured SQL can be corrected.
            String preview = sql;
            if (preview != null && preview.length() > 256) {
                preview = preview.substring(0, 256);
            }
            log.warn("Ledger count SQL execute failed, return 0. sql={}", preview, ex);
            return 0L;
        }
    }

    private NephrologyTimeRangeUtils.DateRange resolveRange(String type, String value) {
        String resolvedType = StringUtils.defaultIfBlank(type, "year");
        String resolvedValue = StringUtils.defaultIfBlank(value, String.valueOf(Year.now().getValue()));
        NephrologyTimeRangeUtils.DateRange range = NephrologyTimeRangeUtils.parse(resolvedType, resolvedValue);
        if (range != null) {
            return range;
        }
        return NephrologyTimeRangeUtils.parse("year", String.valueOf(Year.now().getValue()));
    }

    private Map<String, Object> buildQueryParams(NephrologyTimeRangeUtils.DateRange range, NephrologyLedgerDetailBo bo) {
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", range == null ? null : range.start());
        params.put("endTime", range == null ? null : range.end());
        if (bo != null) {
            params.put("medicalRecordNo", StringUtils.isNotBlank(bo.getMedicalRecordNo()) ? bo.getMedicalRecordNo() : null);
            params.put("idCardNo", StringUtils.isNotBlank(bo.getIdCardNo()) ? bo.getIdCardNo() : null);
        }
        return params;
    }

    private TableDataInfo<NephrologyLedgerDetailVo> queryDetailBySql(NephrologyLedgerQuery queryConfig,
                                                                     NephrologyLedgerDetailBo bo,
                                                                     PageQuery pageQuery,
                                                                     NephrologyTimeRangeUtils.DateRange range) {
        if (queryConfig == null || StringUtils.isBlank(queryConfig.getDetailSql())) {
            return new TableDataInfo<>(List.of(), 0);
        }
        Map<String, Object> params = buildQueryParams(range, bo);
        long total = executeCountSql(queryConfig.getCountSql(), params);
        if (total == 0L) {
            return new TableDataInfo<>(List.of(), 0);
        }
        int pageNum = pageQuery == null || pageQuery.getPageNum() == null ? PageQuery.DEFAULT_PAGE_NUM : pageQuery.getPageNum();
        int pageSize = pageQuery == null || pageQuery.getPageSize() == null ? PageQuery.DEFAULT_PAGE_SIZE : pageQuery.getPageSize();
        int offset = Math.max(pageNum - 1, 0) * pageSize;
        params.put("offset", offset);
        params.put("pageSize", pageSize);
        String detailSql = StringUtils.defaultString(queryConfig.getDetailSql()).trim();
        String pagedSql = detailSql + " LIMIT :offset, :pageSize";
        List<NephrologyLedgerDetailVo> rows = namedParameterJdbcTemplate.query(
            pagedSql,
            params,
            new BeanPropertyRowMapper<>(NephrologyLedgerDetailVo.class));
        return new TableDataInfo<>(rows, total);
    }

    private long countComplication(NephrologyTimeRangeUtils.DateRange range) {
        LambdaQueryWrapper<HemodialysisComplicationPatient> lqw = Wrappers.lambdaQuery();
        lqw.eq(HemodialysisComplicationPatient::getIsDeleted, 0);
        lqw.between(range != null, HemodialysisComplicationPatient::getDischargeTime,
            range == null ? null : range.start(), range == null ? null : range.end());
        return complicationPatientMapper.selectCount(lqw);
    }

    private long countHdDischarge(NephrologyTimeRangeUtils.DateRange range) {
        LambdaQueryWrapper<HdPatient> lqw = Wrappers.lambdaQuery();
        lqw.isNotNull(HdPatient::getDischargeDate);
        lqw.between(range != null, HdPatient::getDischargeDate,
            range == null ? null : range.start(), range == null ? null : range.end());
        return hdPatientMapper.selectCount(lqw);
    }

    private TableDataInfo<NephrologyLedgerDetailVo> queryComplicationDetails(NephrologyLedgerDetailBo bo, PageQuery pageQuery,
                                                                              NephrologyTimeRangeUtils.DateRange range) {
        LambdaQueryWrapper<HemodialysisComplicationPatient> lqw = Wrappers.lambdaQuery();
        lqw.eq(HemodialysisComplicationPatient::getIsDeleted, 0);
        lqw.eq(StringUtils.isNotBlank(bo.getMedicalRecordNo()),
            HemodialysisComplicationPatient::getMedicalRecordNo, bo.getMedicalRecordNo());
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardNo()),
            HemodialysisComplicationPatient::getIdCardNo, bo.getIdCardNo());
        lqw.between(range != null, HemodialysisComplicationPatient::getDischargeTime,
            range == null ? null : range.start(), range == null ? null : range.end());
        lqw.orderByDesc(HemodialysisComplicationPatient::getDischargeTime);
        lqw.orderByDesc(HemodialysisComplicationPatient::getId);

        Page<HemodialysisComplicationPatient> page = complicationPatientMapper.selectPage(pageQuery.build(), lqw);
        List<NephrologyLedgerDetailVo> rows = page.getRecords().stream().map(this::toComplicationDetailVo).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    private TableDataInfo<NephrologyLedgerDetailVo> queryHdDischargeDetails(NephrologyLedgerDetailBo bo, PageQuery pageQuery,
                                                                             NephrologyTimeRangeUtils.DateRange range) {
        LambdaQueryWrapper<HdPatient> lqw = Wrappers.lambdaQuery();
        lqw.and(StringUtils.isNotBlank(bo.getMedicalRecordNo()), wrapper -> wrapper
            .eq(HdPatient::getInpatientNo, bo.getMedicalRecordNo())
            .or()
            .eq(HdPatient::getOutpatientNo, bo.getMedicalRecordNo()));
        lqw.eq(StringUtils.isNotBlank(bo.getIdCardNo()), HdPatient::getIdCard, bo.getIdCardNo());
        lqw.isNotNull(HdPatient::getDischargeDate);
        lqw.between(range != null, HdPatient::getDischargeDate,
            range == null ? null : range.start(), range == null ? null : range.end());
        lqw.orderByDesc(HdPatient::getDischargeDate);
        lqw.orderByDesc(HdPatient::getId);

        Page<HdPatient> page = hdPatientMapper.selectPage(pageQuery.build(), lqw);
        List<NephrologyLedgerDetailVo> rows = page.getRecords().stream().map(this::toHdDischargeDetailVo).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    private NephrologyLedgerDetailVo toComplicationDetailVo(HemodialysisComplicationPatient item) {
        NephrologyLedgerDetailVo vo = new NephrologyLedgerDetailVo();
        vo.setId(item.getId());
        vo.setMedicalRecordNo(item.getMedicalRecordNo());
        vo.setPatientName(item.getPatientName());
        vo.setIdCardNo(item.getIdCardNo());
        vo.setDischargeDepartment(item.getDischargeDepartment());
        vo.setDischargeTime(item.getDischargeTime());
        vo.setDiagnosisName(item.getComplicationDiagnosis());
        vo.setDiagnosisCode(item.getComplicationCode());
        vo.setSourceType("COMPLICATION");
        return vo;
    }

    private NephrologyLedgerDetailVo toHdDischargeDetailVo(HdPatient item) {
        NephrologyLedgerDetailVo vo = new NephrologyLedgerDetailVo();
        vo.setId(item.getId());
        vo.setMedicalRecordNo(StringUtils.isNotBlank(item.getInpatientNo()) ? item.getInpatientNo() : item.getOutpatientNo());
        vo.setPatientName(item.getName());
        vo.setIdCardNo(item.getIdCard());
        vo.setDischargeDepartment(item.getDischargeDept());
        vo.setDischargeTime(item.getDischargeDate());
        vo.setDiagnosisName(item.getDiagName());
        vo.setDiagnosisCode(item.getDiagCode());
        vo.setSourceType("HD_DISCHARGE");
        return vo;
    }

    private void aggregateCount(NephrologyLedgerCountVo node) {
        List<NephrologyLedgerCountVo> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            long count = parseCount(node.getCountValue());
            if (NephrologyLedgerConstants.NODE_TYPE_NUMERATOR.equals(node.getNodeType())) {
                node.setNumeratorCount(count);
                node.setDenominatorCount(0L);
            } else if (NephrologyLedgerConstants.NODE_TYPE_DENOMINATOR.equals(node.getNodeType())) {
                node.setNumeratorCount(0L);
                node.setDenominatorCount(count);
            } else {
                node.setNumeratorCount(count);
                node.setDenominatorCount(0L);
            }
            node.setCountValue(formatCountValue(node.getQueryTarget(), node.getNumeratorCount(), node.getDenominatorCount()));
            return;
        }

        long numerator = 0L;
        long denominator = 0L;
        for (NephrologyLedgerCountVo child : children) {
            aggregateCount(child);
            if (NephrologyLedgerConstants.NODE_TYPE_NUMERATOR.equals(child.getNodeType())) {
                numerator += Objects.requireNonNullElse(child.getNumeratorCount(), 0L);
            } else if (NephrologyLedgerConstants.NODE_TYPE_DENOMINATOR.equals(child.getNodeType())) {
                denominator += Objects.requireNonNullElse(child.getDenominatorCount(), 0L);
            } else {
                numerator += Objects.requireNonNullElse(child.getNumeratorCount(), 0L);
                denominator += Objects.requireNonNullElse(child.getDenominatorCount(), 0L);
            }
        }
        node.setNumeratorCount(numerator);
        node.setDenominatorCount(denominator);
        node.setCountValue(formatCountValue(node.getQueryTarget(), numerator, denominator));
        node.setQueryable(false);
    }

    private long parseCount(String countValue) {
        if (StringUtils.isBlank(countValue)) {
            return 0L;
        }
        try {
            return Long.parseLong(countValue);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private String formatCountValue(String queryTarget, long numerator, long denominator) {
        if (!NephrologyLedgerConstants.QUERY_TARGET_NONE.equals(queryTarget)) {
            return String.valueOf(denominator > 0 ? denominator : numerator);
        }
        if (denominator <= 0) {
            return String.valueOf(numerator);
        }
        BigDecimal percent = BigDecimal.valueOf(numerator)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
        return percent.stripTrailingZeros().toPlainString() + "%";
    }

    private void sortTree(List<NephrologyLedgerCountVo> list, Comparator<NephrologyLedgerCountVo> comparator) {
        list.sort(comparator);
        for (NephrologyLedgerCountVo item : list) {
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                sortTree(item.getChildren(), comparator);
            }
        }
    }
}
