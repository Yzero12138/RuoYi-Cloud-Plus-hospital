package org.dromara.datacenter.hospitalqc.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.datacenter.hospitalqc.config.HospitalQcProperties;
import org.dromara.datacenter.hospitalqc.constants.HospitalQcConstants;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcDataSource;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerItem;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerQuery;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDetailQueryBo;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcDataSourceMapper;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcLedgerItemMapper;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcLedgerQueryMapper;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDetailService;
import org.dromara.datacenter.hospitalqc.util.HospitalQcDeptPermissionUtils;
import org.dromara.datacenter.hospitalqc.util.HospitalQcJdbcExecutor;
import org.dromara.datacenter.hospitalqc.util.HospitalQcPasswordCrypto;
import org.dromara.datacenter.hospitalqc.util.HospitalQcSqlSecurityUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Detail data query service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
@Slf4j
public class HospitalQcDetailServiceImpl implements IHospitalQcDetailService {

    private final HospitalQcLedgerItemMapper ledgerItemMapper;
    private final HospitalQcLedgerQueryMapper ledgerQueryMapper;
    private final HospitalQcDataSourceMapper dataSourceMapper;
    private final HospitalQcPasswordCrypto passwordCrypto;
    private final HospitalQcJdbcExecutor jdbcExecutor;
    private final HospitalQcProperties properties;

    @Override
    public Page<Map<String, Object>> queryDetailPage(HospitalQcDetailQueryBo bo) {
        // Validate parameters
        if (StringUtils.isBlank(bo.getLedgerCode())) {
            throw new ServiceException("台账编码不能为空");
        }
        if (bo.getStartTime() == null || bo.getEndTime() == null) {
            throw new ServiceException("开始时间和结束时间不能为空");
        }

        // Apply department permission filter
        Set<Long> filteredDeptIds = HospitalQcDeptPermissionUtils.validateAndFilterDeptIds(bo.getDeptIds());
        Long deptId = filteredDeptIds.isEmpty() ? null : filteredDeptIds.iterator().next();

        // Get ledger item to find query code
        HospitalQcLedgerItem ledgerItem = ledgerItemMapper.selectOne(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .eq(HospitalQcLedgerItem::getLedgerCode, bo.getLedgerCode())
            .eq(HospitalQcLedgerItem::getNodeType, HospitalQcConstants.NODE_TYPE_INDICATOR)
            .eq(HospitalQcLedgerItem::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .last("limit 1"));

        if (ledgerItem == null) {
            throw new ServiceException("台账不存在或已停用: " + bo.getLedgerCode());
        }

        // Check department permission on ledger item
        HospitalQcDeptPermissionUtils.assertDeptPermission(ledgerItem.getDeptId());

        // Find detail query config
        HospitalQcLedgerQuery queryConfig = findDetailQueryConfig(ledgerItem.getId(), deptId, bo.getNodeType());
        if (queryConfig == null || StringUtils.isBlank(queryConfig.getDetailSql())) {
            throw new ServiceException("该台账未配置明细查询");
        }

        // Validate detail SQL security
        HospitalQcSqlSecurityUtils.validateDetailSql(queryConfig.getDetailSql(), "明细SQL");

        // Get datasource
        HospitalQcDataSource datasource = dataSourceMapper.selectById(queryConfig.getDatasourceId());
        if (datasource == null || datasource.getStatus() == null || datasource.getStatus() != HospitalQcConstants.STATUS_NORMAL) {
            throw new ServiceException("数据源不存在或已停用");
        }

        // Decrypt password
        String plainPassword;
        try {
            plainPassword = passwordCrypto.decrypt(datasource.getPasswordCipher());
        } catch (Exception ex) {
            log.error("Decrypt datasource password failed. datasourceId={}", datasource.getId(), ex);
            throw new ServiceException("数据源密码解密失败");
        }

        // Prepare parameters
        Map<String, Object> params = buildSqlParams(bo.getStartTime(), bo.getEndTime(), deptId, queryConfig.getDeptName());

        // Execute query with pagination
        int timeout = resolveSqlTimeout();
        int pageNum = bo.getPageNum() == null || bo.getPageNum() < 1 ? 1 : bo.getPageNum();
        int pageSize = bo.getPageSize() == null || bo.getPageSize() < 1 ? 20 : Math.min(bo.getPageSize(), 1000);
        int offset = (pageNum - 1) * pageSize;

        try {
            return jdbcExecutor.executeInSession(datasource, plainPassword, timeout, session -> {
                // Get total count first
                long total = 0L;
                try {
                    total = jdbcExecutor.queryForCount(session, queryConfig.getDetailSql(), params);
                } catch (Exception ex) {
                    log.warn("Execute detail count query failed", ex);
                }

                // Get page data
                List<Map<String, Object>> records = jdbcExecutor.queryForPage(
                    session, queryConfig.getDetailSql(), params, offset, pageSize);

                // Apply field mapping if configured
                String fieldMappingJson = queryConfig.getDetailFieldMapping();
                if (StringUtils.isNotBlank(fieldMappingJson)) {
                    List<FieldMappingItem> mappings = parseFieldMappings(fieldMappingJson);
                    List<FieldMappingItem> visible = mappings.stream()
                        .filter(FieldMappingItem::isVisible).collect(Collectors.toList());
                    if (!visible.isEmpty()) {
                        records = records.stream()
                            .map(r -> applyFieldMapping(r, visible))
                            .collect(Collectors.toList());
                    }
                }

                Page<Map<String, Object>> page = new Page<>(pageNum, pageSize, total);
                page.setRecords(records);
                return page;
            });
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Execute detail query failed", ex);
            throw new ServiceException("明细查询执行失败: " + ex.getMessage());
        }
    }

    @Override
    public List<String> queryDetailColumns(String ledgerCode, String nodeType, Long deptId) {
        if (StringUtils.isBlank(ledgerCode)) {
            return List.of();
        }

        HospitalQcLedgerItem ledgerItem = ledgerItemMapper.selectOne(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .eq(HospitalQcLedgerItem::getLedgerCode, ledgerCode)
            .eq(HospitalQcLedgerItem::getNodeType, HospitalQcConstants.NODE_TYPE_INDICATOR)
            .eq(HospitalQcLedgerItem::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .last("limit 1"));

        if (ledgerItem == null) {
            return List.of();
        }

        HospitalQcLedgerQuery queryConfig = findDetailQueryConfig(ledgerItem.getId(), deptId, nodeType);
        if (queryConfig == null || StringUtils.isBlank(queryConfig.getDetailSql())) {
            return List.of();
        }

        // If field mapping is configured, return visible labels in order
        String fieldMappingJson = queryConfig.getDetailFieldMapping();
        if (StringUtils.isNotBlank(fieldMappingJson)) {
            List<FieldMappingItem> mappings = parseFieldMappings(fieldMappingJson);
            return mappings.stream()
                .filter(FieldMappingItem::isVisible)
                .map(FieldMappingItem::getLabel)
                .collect(Collectors.toList());
        }

        // Fallback: extract column aliases from SQL
        return extractColumnNames(queryConfig.getDetailSql());
    }

    /**
     * Find detail query config for a ledger item.
     * First try to find by deptId, then fallback to global config (deptId is null).
     *
     * @param ledgerItemId ledger item ID
     * @param deptId       department ID (optional)
     * @param nodeType     node type filter: N=numerator, D=denominator, null=any
     */
    private HospitalQcLedgerQuery findDetailQueryConfig(Long ledgerItemId, Long deptId, String nodeType) {
        // Get child nodes, optionally filtered by nodeType
        var queryWrapper = Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .eq(HospitalQcLedgerItem::getParentId, ledgerItemId)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED);
        if (StringUtils.isNotBlank(nodeType)) {
            queryWrapper.eq(HospitalQcLedgerItem::getNodeType, nodeType);
        }
        List<HospitalQcLedgerItem> children = ledgerItemMapper.selectList(queryWrapper);

        // Collect all query codes from children
        List<String> queryCodes = new ArrayList<>();
        for (HospitalQcLedgerItem child : children) {
            if (StringUtils.isNotBlank(child.getQueryCode())
                && !HospitalQcConstants.QUERY_CODE_NONE.equals(child.getQueryCode())) {
                queryCodes.add(child.getQueryCode());
            }
        }

        if (queryCodes.isEmpty()) {
            return null;
        }

        // Try to find query config by deptId first
        if (deptId != null) {
            List<HospitalQcLedgerQuery> deptQueries = ledgerQueryMapper.selectList(
                Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
                    .in(HospitalQcLedgerQuery::getQueryCode, queryCodes)
                    .eq(HospitalQcLedgerQuery::getDeptId, deptId)
                    .eq(HospitalQcLedgerQuery::getStatus, HospitalQcConstants.STATUS_NORMAL)
                    .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED));
            if (!deptQueries.isEmpty()) {
                return deptQueries.get(0);
            }
        }

        // Fallback to global config (deptId is null)
        List<HospitalQcLedgerQuery> globalQueries = ledgerQueryMapper.selectList(
            Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
                .in(HospitalQcLedgerQuery::getQueryCode, queryCodes)
                .isNull(HospitalQcLedgerQuery::getDeptId)
                .eq(HospitalQcLedgerQuery::getStatus, HospitalQcConstants.STATUS_NORMAL)
                .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED));

        return globalQueries.isEmpty() ? null : globalQueries.get(0);
    }

    /**
     * Extract column names from SELECT SQL.
     * This is a simple parser that handles basic column aliases.
     */
    private List<String> extractColumnNames(String sql) {
        List<String> columns = new ArrayList<>();
        if (StringUtils.isBlank(sql)) {
            return columns;
        }

        // Simple parsing: look for patterns like "column as alias" or "column alias"
        String upperSql = sql.toUpperCase();
        int selectIdx = upperSql.indexOf("SELECT");
        int fromIdx = upperSql.indexOf("FROM");

        if (selectIdx < 0 || fromIdx < 0 || fromIdx <= selectIdx) {
            return columns;
        }

        String selectPart = sql.substring(selectIdx + 6, fromIdx);
        String[] parts = selectPart.split(",");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) {
                continue;
            }

            // Try to find alias
            String alias = null;

            // Pattern: ... AS alias
            int asIdx = part.toUpperCase().lastIndexOf(" AS ");
            if (asIdx > 0) {
                alias = part.substring(asIdx + 4).trim();
            } else {
                // Pattern: ... alias (last word)
                int spaceIdx = part.lastIndexOf(' ');
                if (spaceIdx > 0) {
                    String lastWord = part.substring(spaceIdx + 1).trim();
                    // Check if it's a simple identifier (not a function call)
                    if (lastWord.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                        alias = lastWord;
                    }
                }
            }

            // If no alias found, use the whole expression (simplified)
            if (alias == null) {
                // Remove table prefix if exists
                int dotIdx = part.lastIndexOf('.');
                if (dotIdx > 0 && dotIdx < part.length() - 1) {
                    alias = part.substring(dotIdx + 1).trim();
                } else {
                    alias = part;
                }
            }

            if (StringUtils.isNotBlank(alias)) {
                columns.add(alias);
            }
        }

        return columns;
    }

    private Map<String, Object> buildSqlParams(Date startTime, Date endTime, Long deptId, String deptName) {
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("deptId", deptId);
        params.put("deptName", deptName);
        return params;
    }

    private int resolveSqlTimeout() {
        Integer timeout = properties.getSqlTimeoutSeconds();
        return timeout == null || timeout <= 0 ? HospitalQcConstants.DEFAULT_SQL_TIMEOUT_SECONDS : timeout;
    }

    private List<FieldMappingItem> parseFieldMappings(String json) {
        try {
            List<FieldMappingItem> result = JSON.parseArray(json, FieldMappingItem.class);
            return result == null ? List.of() : result;
        } catch (Exception ex) {
            log.warn("Failed to parse detailFieldMapping JSON: {}", ex.getMessage());
            return List.of();
        }
    }

    private Map<String, Object> applyFieldMapping(Map<String, Object> record, List<FieldMappingItem> visibleMappings) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (FieldMappingItem item : visibleMappings) {
            if (record.containsKey(item.getField())) {
                result.put(item.getLabel(), record.get(item.getField()));
            }
        }
        return result;
    }

    @lombok.Data
    private static class FieldMappingItem {
        private String field;
        private String label;
        private boolean visible = true;
    }
}
