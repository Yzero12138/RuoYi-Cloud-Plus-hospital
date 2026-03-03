package org.dromara.datacenter.hospitalqc.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.datacenter.hospitalqc.config.HospitalQcProperties;
import org.dromara.datacenter.hospitalqc.constants.HospitalQcConstants;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcDataSource;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerItem;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerQuery;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDashboardQueryBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcReportQueryBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDashboardFilterVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDashboardKpiVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDashboardOverviewVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDeptRankingVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcOptionVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcQuarterMetricVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcReportRowVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcTrendSeriesVo;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcDataSourceMapper;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcLedgerItemMapper;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcLedgerQueryMapper;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDashboardService;
import org.dromara.datacenter.hospitalqc.util.HospitalQcJdbcExecutor;
import org.dromara.datacenter.hospitalqc.util.HospitalQcPasswordCrypto;
import org.dromara.datacenter.hospitalqc.util.HospitalQcTimeRangeUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Dashboard and report statistics service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
@Slf4j
public class HospitalQcDashboardServiceImpl implements IHospitalQcDashboardService {

    private static final String NO_DENOMINATOR_RULE = "当无分母或分母=0时：百分比和百分点变化显示--，环比增长统一按分子数量计算。";

    private final HospitalQcLedgerItemMapper ledgerItemMapper;
    private final HospitalQcLedgerQueryMapper ledgerQueryMapper;
    private final HospitalQcDataSourceMapper dataSourceMapper;
    private final HospitalQcPasswordCrypto passwordCrypto;
    private final HospitalQcJdbcExecutor jdbcExecutor;
    private final HospitalQcProperties properties;

    private final Map<String, CachedCount> countCache = new ConcurrentHashMap<>();

    @Override
    public HospitalQcDashboardFilterVo selectFilterOptions() {
        List<HospitalQcLedgerItem> ledgerItems = ledgerItemMapper.selectList(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .select(HospitalQcLedgerItem::getLedgerCode, HospitalQcLedgerItem::getLedgerName)
            .eq(HospitalQcLedgerItem::getNodeType, HospitalQcConstants.NODE_TYPE_INDICATOR)
            .eq(HospitalQcLedgerItem::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .orderByAsc(HospitalQcLedgerItem::getSortOrder)
            .orderByAsc(HospitalQcLedgerItem::getId));
        List<HospitalQcOptionVo> ledgerOptions = ledgerItems.stream()
            .map(it -> new HospitalQcOptionVo(it.getLedgerCode(), it.getLedgerName()))
            .toList();

        List<HospitalQcLedgerQuery> queryItems = ledgerQueryMapper.selectList(Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
            .select(HospitalQcLedgerQuery::getDeptId, HospitalQcLedgerQuery::getDeptName)
            .eq(HospitalQcLedgerQuery::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .orderByAsc(HospitalQcLedgerQuery::getDeptName)
            .orderByAsc(HospitalQcLedgerQuery::getDeptId));

        Map<Long, String> deptMap = new LinkedHashMap<>();
        for (HospitalQcLedgerQuery item : queryItems) {
            if (item.getDeptId() != null) {
                deptMap.putIfAbsent(item.getDeptId(), item.getDeptName());
            }
        }
        List<HospitalQcOptionVo> deptOptions = deptMap.entrySet().stream()
            .map(entry -> new HospitalQcOptionVo(String.valueOf(entry.getKey()), entry.getValue()))
            .toList();

        HospitalQcDashboardFilterVo vo = new HospitalQcDashboardFilterVo();
        vo.setDeptOptions(deptOptions);
        vo.setLedgerOptions(ledgerOptions);
        return vo;
    }

    @Override
    public HospitalQcDashboardOverviewVo dashboardOverview(HospitalQcDashboardQueryBo bo) {
        int year = HospitalQcTimeRangeUtils.resolveYear(bo.getYear());
        List<HospitalQcLedgerItem> allItems = loadActiveLedgerItems();
        List<IndicatorMeta> indicators = buildIndicatorMetas(allItems, bo.getLedgerCodes());
        if (indicators.isEmpty()) {
            return emptyDashboard(year);
        }

        Set<String> queryCodes = indicators.stream()
            .flatMap(item -> item.allQueryCodes().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<HospitalQcLedgerQuery> queryConfigs = loadActiveQueryConfigs(queryCodes);
        if (queryConfigs.isEmpty()) {
            return emptyDashboard(year);
        }

        List<DeptContext> deptContexts = resolveDeptContexts(bo.getDeptIds(), queryConfigs);
        if (deptContexts.isEmpty()) {
            return emptyDashboard(year);
        }

        Map<DeptQueryKey, HospitalQcLedgerQuery> queryMap = buildDeptQueryMap(queryConfigs);
        List<HospitalQcTimeRangeUtils.DateRange> quarterRanges = HospitalQcTimeRangeUtils.buildQuarterRanges(year);
        Map<String, Long> countMap = loadCountMap(deptContexts, queryCodes, quarterRanges, queryMap, true);

        List<HospitalQcTrendSeriesVo> trends = new ArrayList<>();
        for (IndicatorMeta indicator : indicators) {
            List<HospitalQcQuarterMetricVo> metrics = buildQuarterMetrics(indicator, deptContexts, quarterRanges, countMap);
            HospitalQcTrendSeriesVo trendSeries = new HospitalQcTrendSeriesVo();
            trendSeries.setLedgerCode(indicator.ledgerCode());
            trendSeries.setLedgerName(indicator.ledgerName());
            trendSeries.setQuarterMetrics(metrics);
            trends.add(trendSeries);
        }

        HospitalQcDashboardOverviewVo overview = new HospitalQcDashboardOverviewVo();
        overview.setYear(year);
        overview.setQuarters(quarterRanges.stream().map(HospitalQcTimeRangeUtils.DateRange::label).toList());
        overview.setTrends(trends);
        overview.setKpi(buildKpi(trends));
        overview.setRankings(buildRankings(indicators, deptContexts, quarterRanges, countMap));
        overview.setNoDenominatorRule(NO_DENOMINATOR_RULE);
        return overview;
    }

    @Override
    public List<HospitalQcReportRowVo> reportSummary(HospitalQcReportQueryBo bo) {
        HospitalQcTimeRangeUtils.DateRange currentRange = HospitalQcTimeRangeUtils.parseReportRange(
            StringUtils.defaultIfBlank(bo.getTimeType(), HospitalQcConstants.TIME_TYPE_QUARTER),
            bo.getTimeValue(),
            bo.getStartTime(),
            bo.getEndTime()
        );
        HospitalQcTimeRangeUtils.DateRange previousRange = HospitalQcTimeRangeUtils.previousRange(currentRange);

        List<HospitalQcLedgerItem> allItems = loadActiveLedgerItems();
        List<IndicatorMeta> indicators = buildIndicatorMetas(allItems, bo.getLedgerCodes());
        if (indicators.isEmpty()) {
            return List.of();
        }

        Set<String> queryCodes = indicators.stream()
            .flatMap(item -> item.allQueryCodes().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<HospitalQcLedgerQuery> queryConfigs = loadActiveQueryConfigs(queryCodes);
        if (queryConfigs.isEmpty()) {
            return List.of();
        }

        List<DeptContext> deptContexts = resolveDeptContexts(bo.getDeptIds(), queryConfigs);
        if (deptContexts.isEmpty()) {
            return List.of();
        }

        Map<DeptQueryKey, HospitalQcLedgerQuery> queryMap = buildDeptQueryMap(queryConfigs);
        List<HospitalQcTimeRangeUtils.DateRange> ranges = List.of(currentRange, previousRange);
        Map<String, Long> countMap = loadCountMap(deptContexts, queryCodes, ranges, queryMap, false);

        List<HospitalQcReportRowVo> rows = new ArrayList<>();
        for (DeptContext dept : deptContexts) {
            for (IndicatorMeta indicator : indicators) {
                MetricRaw currentRaw = aggregateMetric(indicator, List.of(dept), currentRange.label(), countMap);
                MetricRaw previousRaw = aggregateMetric(indicator, List.of(dept), previousRange.label(), countMap);
                GrowthResult growth = calcGrowth(currentRaw, previousRaw);

                HospitalQcReportRowVo row = new HospitalQcReportRowVo();
                row.setPeriodLabel(currentRange.label());
                row.setDeptId(dept.deptId());
                row.setDeptName(dept.deptName());
                row.setLedgerCode(indicator.ledgerCode());
                row.setLedgerName(indicator.ledgerName());
                row.setNumerator(currentRaw.numerator());
                row.setDenominator(currentRaw.denominator());
                row.setIndicatorPercent(currentRaw.percent());
                row.setQoqGrowth(growth.qoqGrowth());
                row.setPercentagePointChange(growth.pointChange());
                row.setNoDenominator(currentRaw.noDenominator());
                row.setPercentDisplay(toPercentDisplay(currentRaw.percent()));
                row.setGrowthDisplay(toGrowthDisplay(growth.qoqGrowth()));
                row.setPointChangeDisplay(toPointChangeDisplay(growth.pointChange()));
                rows.add(row);
            }
        }

        rows.sort(Comparator
            .comparing(HospitalQcReportRowVo::getDeptName, Comparator.nullsLast(String::compareTo))
            .thenComparing(HospitalQcReportRowVo::getLedgerCode, Comparator.nullsLast(String::compareTo)));
        return rows;
    }

    private List<HospitalQcLedgerItem> loadActiveLedgerItems() {
        return ledgerItemMapper.selectList(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .eq(HospitalQcLedgerItem::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .orderByAsc(HospitalQcLedgerItem::getSortOrder)
            .orderByAsc(HospitalQcLedgerItem::getId));
    }

    private List<HospitalQcLedgerQuery> loadActiveQueryConfigs(Collection<String> queryCodes) {
        if (queryCodes == null || queryCodes.isEmpty()) {
            return List.of();
        }
        return ledgerQueryMapper.selectList(Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
            .in(HospitalQcLedgerQuery::getQueryCode, queryCodes)
            .eq(HospitalQcLedgerQuery::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED));
    }

    private List<IndicatorMeta> buildIndicatorMetas(List<HospitalQcLedgerItem> allItems, List<String> selectedLedgerCodes) {
        Set<String> selected = selectedLedgerCodes == null ? Set.of() : new HashSet<>(selectedLedgerCodes);
        Map<Long, List<HospitalQcLedgerItem>> childrenMap = new HashMap<>();
        for (HospitalQcLedgerItem item : allItems) {
            childrenMap.computeIfAbsent(Objects.requireNonNullElse(item.getParentId(), 0L), key -> new ArrayList<>()).add(item);
        }

        List<HospitalQcLedgerItem> indicators = allItems.stream()
            .filter(item -> HospitalQcConstants.NODE_TYPE_INDICATOR.equals(item.getNodeType()))
            .filter(item -> selected.isEmpty() || selected.contains(item.getLedgerCode()))
            .toList();

        List<IndicatorMeta> metas = new ArrayList<>();
        for (HospitalQcLedgerItem indicator : indicators) {
            LinkedHashSet<String> numeratorCodes = new LinkedHashSet<>();
            LinkedHashSet<String> denominatorCodes = new LinkedHashSet<>();
            collectQueryCodes(indicator.getId(), childrenMap, numeratorCodes, denominatorCodes);
            if (numeratorCodes.isEmpty() && denominatorCodes.isEmpty()) {
                continue;
            }
            metas.add(new IndicatorMeta(
                indicator.getLedgerCode(),
                indicator.getLedgerName(),
                new ArrayList<>(numeratorCodes),
                new ArrayList<>(denominatorCodes)
            ));
        }
        return metas;
    }

    private void collectQueryCodes(Long parentId,
                                   Map<Long, List<HospitalQcLedgerItem>> childrenMap,
                                   Set<String> numeratorCodes,
                                   Set<String> denominatorCodes) {
        List<HospitalQcLedgerItem> children = childrenMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return;
        }
        for (HospitalQcLedgerItem child : children) {
            if (HospitalQcConstants.NODE_TYPE_NUMERATOR.equals(child.getNodeType())
                && StringUtils.isNotBlank(child.getQueryCode())
                && !HospitalQcConstants.QUERY_CODE_NONE.equals(child.getQueryCode())) {
                numeratorCodes.add(child.getQueryCode());
            }
            if (HospitalQcConstants.NODE_TYPE_DENOMINATOR.equals(child.getNodeType())
                && StringUtils.isNotBlank(child.getQueryCode())
                && !HospitalQcConstants.QUERY_CODE_NONE.equals(child.getQueryCode())) {
                denominatorCodes.add(child.getQueryCode());
            }
            collectQueryCodes(child.getId(), childrenMap, numeratorCodes, denominatorCodes);
        }
    }

    private List<DeptContext> resolveDeptContexts(List<Long> requestedDeptIds, List<HospitalQcLedgerQuery> queryConfigs) {
        Map<Long, String> deptMap = new LinkedHashMap<>();
        for (HospitalQcLedgerQuery query : queryConfigs) {
            if (query.getDeptId() != null) {
                deptMap.putIfAbsent(query.getDeptId(), query.getDeptName());
            }
        }

        if (requestedDeptIds == null || requestedDeptIds.isEmpty()) {
            return deptMap.entrySet().stream()
                .map(entry -> new DeptContext(entry.getKey(), entry.getValue()))
                .toList();
        }

        List<DeptContext> depts = new ArrayList<>();
        for (Long deptId : requestedDeptIds) {
            if (deptId == null) {
                continue;
            }
            depts.add(new DeptContext(deptId, StringUtils.defaultIfBlank(deptMap.get(deptId), "科室" + deptId)));
        }
        return depts;
    }

    private Map<DeptQueryKey, HospitalQcLedgerQuery> buildDeptQueryMap(List<HospitalQcLedgerQuery> queryConfigs) {
        Map<DeptQueryKey, HospitalQcLedgerQuery> map = new HashMap<>();
        for (HospitalQcLedgerQuery query : queryConfigs) {
            map.put(new DeptQueryKey(query.getDeptId(), query.getQueryCode()), query);
            if (query.getDeptId() == null) {
                map.put(new DeptQueryKey(null, query.getQueryCode()), query);
            }
        }
        return map;
    }

    private Map<String, Long> loadCountMap(List<DeptContext> deptContexts,
                                           Set<String> queryCodes,
                                           List<HospitalQcTimeRangeUtils.DateRange> ranges,
                                           Map<DeptQueryKey, HospitalQcLedgerQuery> queryMap,
                                           boolean enableCache) {
        if (deptContexts.isEmpty() || queryCodes.isEmpty() || ranges.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<QueryTask>> taskByDatasource = new HashMap<>();
        for (DeptContext dept : deptContexts) {
            for (String queryCode : queryCodes) {
                HospitalQcLedgerQuery queryConfig = resolveQueryConfig(queryMap, dept.deptId(), queryCode);
                if (queryConfig == null) {
                    continue;
                }
                for (HospitalQcTimeRangeUtils.DateRange range : ranges) {
                    String countKey = countKey(range.label(), dept.deptId(), queryCode);
                    Map<String, Object> params = buildSqlParams(range.startTime(), range.endTime(), dept.deptId(), dept.deptName());
                    String cacheKey = cacheKey(queryConfig.getDatasourceId(), queryConfig.getCountSql(), countKey);
                    taskByDatasource.computeIfAbsent(queryConfig.getDatasourceId(), key -> new ArrayList<>())
                        .add(new QueryTask(countKey, cacheKey, queryConfig.getCountSql(), params));
                }
            }
        }

        if (taskByDatasource.isEmpty()) {
            return Map.of();
        }

        Map<Long, HospitalQcDataSource> datasourceMap = loadDatasourceMap(taskByDatasource.keySet());
        Map<String, Long> resultMap = new HashMap<>();

        for (Map.Entry<Long, List<QueryTask>> entry : taskByDatasource.entrySet()) {
            Long datasourceId = entry.getKey();
            HospitalQcDataSource datasource = datasourceMap.get(datasourceId);
            if (datasource == null) {
                for (QueryTask task : entry.getValue()) {
                    resultMap.put(task.countKey(), 0L);
                }
                continue;
            }

            String plainPassword;
            try {
                plainPassword = passwordCrypto.decrypt(datasource.getPasswordCipher());
            } catch (Exception ex) {
                log.warn("Decrypt datasource password failed. datasourceId={}", datasourceId, ex);
                for (QueryTask task : entry.getValue()) {
                    resultMap.put(task.countKey(), 0L);
                }
                continue;
            }

            int timeout = resolveSqlTimeout();
            try {
                jdbcExecutor.executeInSession(datasource, plainPassword, timeout, session -> {
                    for (QueryTask task : entry.getValue()) {
                        if (enableCache) {
                            CachedCount cache = countCache.get(task.cacheKey());
                            if (cache != null && cache.expireAt() > System.currentTimeMillis()) {
                                resultMap.put(task.countKey(), cache.countValue());
                                continue;
                            }
                        }
                        long count = 0L;
                        try {
                            count = jdbcExecutor.queryForCount(session, task.countSql(), task.params());
                        } catch (Exception ex) {
                            log.warn("Execute dashboard count sql failed. key={}", task.countKey(), ex);
                        }
                        resultMap.put(task.countKey(), count);
                        if (enableCache) {
                            countCache.put(task.cacheKey(), new CachedCount(count, System.currentTimeMillis() + resolveCacheTtlMs()));
                        }
                    }
                    return null;
                });
            } catch (Exception ex) {
                log.warn("Execute dashboard sql group failed. datasourceId={}", datasourceId, ex);
                for (QueryTask task : entry.getValue()) {
                    resultMap.putIfAbsent(task.countKey(), 0L);
                }
            }
        }

        return resultMap;
    }

    private HospitalQcLedgerQuery resolveQueryConfig(Map<DeptQueryKey, HospitalQcLedgerQuery> queryMap, Long deptId, String queryCode) {
        HospitalQcLedgerQuery deptQuery = queryMap.get(new DeptQueryKey(deptId, queryCode));
        if (deptQuery != null) {
            return deptQuery;
        }
        return queryMap.get(new DeptQueryKey(null, queryCode));
    }

    private Map<Long, HospitalQcDataSource> loadDatasourceMap(Set<Long> datasourceIds) {
        List<HospitalQcDataSource> list = dataSourceMapper.selectList(Wrappers.<HospitalQcDataSource>lambdaQuery()
            .in(HospitalQcDataSource::getId, datasourceIds)
            .eq(HospitalQcDataSource::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED));
        Map<Long, HospitalQcDataSource> map = new HashMap<>();
        for (HospitalQcDataSource dataSource : list) {
            map.put(dataSource.getId(), dataSource);
        }
        return map;
    }

    private List<HospitalQcQuarterMetricVo> buildQuarterMetrics(IndicatorMeta indicator,
                                                                List<DeptContext> deptContexts,
                                                                List<HospitalQcTimeRangeUtils.DateRange> quarterRanges,
                                                                Map<String, Long> countMap) {
        List<MetricRaw> values = new ArrayList<>();
        for (HospitalQcTimeRangeUtils.DateRange range : quarterRanges) {
            values.add(aggregateMetric(indicator, deptContexts, range.label(), countMap));
        }

        List<HospitalQcQuarterMetricVo> metrics = new ArrayList<>();
        for (int i = 0; i < quarterRanges.size(); i++) {
            MetricRaw current = values.get(i);
            MetricRaw previous = i > 0 ? values.get(i - 1) : null;
            GrowthResult growth = calcGrowth(current, previous);

            HospitalQcQuarterMetricVo metric = new HospitalQcQuarterMetricVo();
            metric.setQuarter(quarterRanges.get(i).label());
            metric.setNumerator(current.numerator());
            metric.setDenominator(current.denominator());
            metric.setIndicatorPercent(current.percent());
            metric.setQoqGrowth(growth.qoqGrowth());
            metric.setPercentagePointChange(growth.pointChange());
            metric.setNoDenominator(current.noDenominator());
            metric.setPercentDisplay(toPercentDisplay(current.percent()));
            metric.setGrowthDisplay(toGrowthDisplay(growth.qoqGrowth()));
            metric.setPointChangeDisplay(toPointChangeDisplay(growth.pointChange()));
            metrics.add(metric);
        }
        return metrics;
    }

    private HospitalQcDashboardKpiVo buildKpi(List<HospitalQcTrendSeriesVo> trends) {
        if (trends == null || trends.isEmpty()) {
            HospitalQcDashboardKpiVo vo = new HospitalQcDashboardKpiVo();
            vo.setNumerator(0L);
            vo.setDenominator(0L);
            vo.setPercentDisplay("--");
            vo.setGrowthDisplay("--");
            vo.setPointChangeDisplay("--");
            vo.setNoDenominator(true);
            return vo;
        }

        int latestIndex = trends.get(0).getQuarterMetrics().size() - 1;
        if (latestIndex < 0) {
            return new HospitalQcDashboardKpiVo();
        }
        long latestNumerator = 0L;
        long latestDenominator = 0L;
        long prevNumerator = 0L;
        long prevDenominator = 0L;

        for (HospitalQcTrendSeriesVo trend : trends) {
            if (trend.getQuarterMetrics() == null || trend.getQuarterMetrics().isEmpty()) {
                continue;
            }
            HospitalQcQuarterMetricVo latest = trend.getQuarterMetrics().get(latestIndex);
            latestNumerator += Objects.requireNonNullElse(latest.getNumerator(), 0L);
            latestDenominator += Objects.requireNonNullElse(latest.getDenominator(), 0L);
            if (latestIndex > 0) {
                HospitalQcQuarterMetricVo prev = trend.getQuarterMetrics().get(latestIndex - 1);
                prevNumerator += Objects.requireNonNullElse(prev.getNumerator(), 0L);
                prevDenominator += Objects.requireNonNullElse(prev.getDenominator(), 0L);
            }
        }

        MetricRaw current = buildRaw(latestNumerator, latestDenominator, latestDenominator <= 0);
        MetricRaw previous = buildRaw(prevNumerator, prevDenominator, prevDenominator <= 0);
        GrowthResult growth = calcGrowth(current, previous);

        HospitalQcDashboardKpiVo vo = new HospitalQcDashboardKpiVo();
        vo.setNumerator(latestNumerator);
        vo.setDenominator(latestDenominator);
        vo.setIndicatorPercent(current.percent());
        vo.setQoqGrowth(growth.qoqGrowth());
        vo.setPercentagePointChange(growth.pointChange());
        vo.setNoDenominator(current.noDenominator());
        vo.setPercentDisplay(toPercentDisplay(current.percent()));
        vo.setGrowthDisplay(toGrowthDisplay(growth.qoqGrowth()));
        vo.setPointChangeDisplay(toPointChangeDisplay(growth.pointChange()));
        return vo;
    }

    private List<HospitalQcDeptRankingVo> buildRankings(List<IndicatorMeta> indicators,
                                                        List<DeptContext> deptContexts,
                                                        List<HospitalQcTimeRangeUtils.DateRange> quarterRanges,
                                                        Map<String, Long> countMap) {
        if (quarterRanges.isEmpty()) {
            return List.of();
        }
        String latestPeriod = quarterRanges.get(quarterRanges.size() - 1).label();
        String prevPeriod = quarterRanges.size() > 1 ? quarterRanges.get(quarterRanges.size() - 2).label() : null;

        List<HospitalQcDeptRankingVo> rows = new ArrayList<>();
        for (DeptContext dept : deptContexts) {
            long latestNumerator = 0L;
            long latestDenominator = 0L;
            long prevNumerator = 0L;
            long prevDenominator = 0L;
            boolean hasDenominatorLeaf = false;

            for (IndicatorMeta indicator : indicators) {
                hasDenominatorLeaf = hasDenominatorLeaf || !indicator.denominatorQueryCodes().isEmpty();
                latestNumerator += sumByCodes(latestPeriod, dept.deptId(), indicator.numeratorQueryCodes(), countMap);
                latestDenominator += sumByCodes(latestPeriod, dept.deptId(), indicator.denominatorQueryCodes(), countMap);
                if (prevPeriod != null) {
                    prevNumerator += sumByCodes(prevPeriod, dept.deptId(), indicator.numeratorQueryCodes(), countMap);
                    prevDenominator += sumByCodes(prevPeriod, dept.deptId(), indicator.denominatorQueryCodes(), countMap);
                }
            }

            MetricRaw current = buildRaw(latestNumerator, latestDenominator, !hasDenominatorLeaf || latestDenominator <= 0);
            MetricRaw previous = buildRaw(prevNumerator, prevDenominator, !hasDenominatorLeaf || prevDenominator <= 0);
            GrowthResult growth = calcGrowth(current, previous);

            HospitalQcDeptRankingVo row = new HospitalQcDeptRankingVo();
            row.setDeptId(dept.deptId());
            row.setDeptName(dept.deptName());
            row.setNumerator(latestNumerator);
            row.setDenominator(latestDenominator);
            row.setIndicatorPercent(current.percent());
            row.setQoqGrowth(growth.qoqGrowth());
            row.setPercentagePointChange(growth.pointChange());
            row.setPercentDisplay(toPercentDisplay(current.percent()));
            row.setGrowthDisplay(toGrowthDisplay(growth.qoqGrowth()));
            row.setPointChangeDisplay(toPointChangeDisplay(growth.pointChange()));
            row.setTrend(resolveTrend(growth.qoqGrowth()));
            rows.add(row);
        }

        rows.sort(Comparator
            .comparing(HospitalQcDeptRankingVo::getIndicatorPercent, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(HospitalQcDeptRankingVo::getNumerator, Comparator.nullsLast(Comparator.reverseOrder())));
        return rows;
    }

    private MetricRaw aggregateMetric(IndicatorMeta indicator,
                                      List<DeptContext> depts,
                                      String periodLabel,
                                      Map<String, Long> countMap) {
        long numerator = 0L;
        long denominator = 0L;
        for (DeptContext dept : depts) {
            numerator += sumByCodes(periodLabel, dept.deptId(), indicator.numeratorQueryCodes(), countMap);
            denominator += sumByCodes(periodLabel, dept.deptId(), indicator.denominatorQueryCodes(), countMap);
        }
        boolean noDenominator = indicator.denominatorQueryCodes().isEmpty() || denominator <= 0;
        return buildRaw(numerator, denominator, noDenominator);
    }

    private MetricRaw buildRaw(long numerator, long denominator, boolean noDenominator) {
        BigDecimal percent = null;
        if (!noDenominator && denominator > 0) {
            percent = BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
        }
        return new MetricRaw(numerator, denominator, percent, noDenominator);
    }

    private long sumByCodes(String periodLabel, Long deptId, List<String> queryCodes, Map<String, Long> countMap) {
        long sum = 0L;
        for (String queryCode : queryCodes) {
            sum += countMap.getOrDefault(countKey(periodLabel, deptId, queryCode), 0L);
        }
        return sum;
    }

    private GrowthResult calcGrowth(MetricRaw current, MetricRaw previous) {
        if (previous == null) {
            return new GrowthResult(null, null);
        }
        if (current.noDenominator()) {
            if (previous.numerator() <= 0) {
                return new GrowthResult(null, null);
            }
            BigDecimal growth = BigDecimal.valueOf(current.numerator() - previous.numerator())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous.numerator()), 2, RoundingMode.HALF_UP);
            return new GrowthResult(growth, null);
        }

        if (current.percent() == null || previous.percent() == null) {
            return new GrowthResult(null, null);
        }

        BigDecimal pointChange = current.percent().subtract(previous.percent()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal growth = null;
        if (previous.percent().compareTo(BigDecimal.ZERO) != 0) {
            growth = pointChange.multiply(BigDecimal.valueOf(100))
                .divide(previous.percent().abs(), 2, RoundingMode.HALF_UP);
        }
        return new GrowthResult(growth, pointChange);
    }

    private String resolveTrend(BigDecimal growth) {
        if (growth == null) {
            return "NONE";
        }
        if (growth.compareTo(BigDecimal.ZERO) > 0) {
            return "UP";
        }
        if (growth.compareTo(BigDecimal.ZERO) < 0) {
            return "DOWN";
        }
        return "FLAT";
    }

    private HospitalQcDashboardOverviewVo emptyDashboard(int year) {
        HospitalQcDashboardOverviewVo overview = new HospitalQcDashboardOverviewVo();
        overview.setYear(year);
        overview.setQuarters(List.of(year + "-Q1", year + "-Q2", year + "-Q3", year + "-Q4"));
        overview.setTrends(List.of());
        overview.setRankings(List.of());

        HospitalQcDashboardKpiVo kpi = new HospitalQcDashboardKpiVo();
        kpi.setNumerator(0L);
        kpi.setDenominator(0L);
        kpi.setPercentDisplay("--");
        kpi.setGrowthDisplay("--");
        kpi.setPointChangeDisplay("--");
        kpi.setNoDenominator(true);
        overview.setKpi(kpi);
        overview.setNoDenominatorRule(NO_DENOMINATOR_RULE);
        return overview;
    }

    private Map<String, Object> buildSqlParams(Date startTime, Date endTime, Long deptId, String deptName) {
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("deptId", deptId);
        params.put("deptName", deptName);
        return params;
    }

    private String countKey(String period, Long deptId, String queryCode) {
        return period + "|" + deptId + "|" + queryCode;
    }

    private String cacheKey(Long datasourceId, String countSql, String countKey) {
        return datasourceId + "|" + Objects.hashCode(countSql) + "|" + countKey;
    }

    private String toPercentDisplay(BigDecimal percent) {
        if (percent == null) {
            return "--";
        }
        return percent.stripTrailingZeros().toPlainString() + "%";
    }

    private String toGrowthDisplay(BigDecimal value) {
        if (value == null) {
            return "--";
        }
        String prefix = value.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return prefix + value.stripTrailingZeros().toPlainString() + "%";
    }

    private String toPointChangeDisplay(BigDecimal value) {
        if (value == null) {
            return "--";
        }
        String prefix = value.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return prefix + value.stripTrailingZeros().toPlainString() + "pp";
    }

    private int resolveSqlTimeout() {
        Integer timeout = properties.getSqlTimeoutSeconds();
        return timeout == null || timeout <= 0 ? HospitalQcConstants.DEFAULT_SQL_TIMEOUT_SECONDS : timeout;
    }

    private long resolveCacheTtlMs() {
        Integer seconds = properties.getDashboardCacheSeconds();
        int value = seconds == null || seconds <= 0 ? HospitalQcConstants.DEFAULT_CACHE_SECONDS : seconds;
        return value * 1000L;
    }

    private record IndicatorMeta(
        String ledgerCode,
        String ledgerName,
        List<String> numeratorQueryCodes,
        List<String> denominatorQueryCodes
    ) {
        Set<String> allQueryCodes() {
            Set<String> set = new LinkedHashSet<>(numeratorQueryCodes);
            set.addAll(denominatorQueryCodes);
            return set;
        }
    }

    private record DeptContext(Long deptId, String deptName) {
    }

    private record DeptQueryKey(Long deptId, String queryCode) {
    }

    private record QueryTask(String countKey, String cacheKey, String countSql, Map<String, Object> params) {
    }

    private record MetricRaw(Long numerator, Long denominator, BigDecimal percent, Boolean noDenominator) {
    }

    private record GrowthResult(BigDecimal qoqGrowth, BigDecimal pointChange) {
    }

    private record CachedCount(Long countValue, Long expireAt) {
    }
}

