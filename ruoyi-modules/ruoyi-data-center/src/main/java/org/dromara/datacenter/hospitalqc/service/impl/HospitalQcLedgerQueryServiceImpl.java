package org.dromara.datacenter.hospitalqc.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.hospitalqc.config.HospitalQcProperties;
import org.dromara.datacenter.hospitalqc.constants.HospitalQcConstants;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcDataSource;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerQuery;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerQueryBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerQueryTestBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcLedgerQueryVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcOptionVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcSqlTestResultVo;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcDataSourceMapper;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcLedgerQueryMapper;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcLedgerQueryService;
import org.dromara.datacenter.hospitalqc.util.HospitalQcJdbcExecutor;
import org.dromara.datacenter.hospitalqc.util.HospitalQcPasswordCrypto;
import org.dromara.datacenter.hospitalqc.util.HospitalQcSqlSecurityUtils;
import org.dromara.datacenter.hospitalqc.util.HospitalQcTimeRangeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ledger SQL query config service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class HospitalQcLedgerQueryServiceImpl implements IHospitalQcLedgerQueryService {

    private final HospitalQcLedgerQueryMapper baseMapper;
    private final HospitalQcDataSourceMapper dataSourceMapper;
    private final HospitalQcPasswordCrypto passwordCrypto;
    private final HospitalQcJdbcExecutor jdbcExecutor;
    private final HospitalQcProperties properties;

    @Override
    public TableDataInfo<HospitalQcLedgerQueryVo> selectPageList(HospitalQcLedgerQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HospitalQcLedgerQuery> lqw = buildQueryWrapper(bo);
        Page<HospitalQcLedgerQueryVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public HospitalQcLedgerQueryVo selectById(Long id) {
        return baseMapper.selectVoOne(Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
            .eq(HospitalQcLedgerQuery::getId, id)
            .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED));
    }

    @Override
    public List<HospitalQcOptionVo> selectOptions() {
        List<HospitalQcLedgerQuery> list = baseMapper.selectList(Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
            .select(HospitalQcLedgerQuery::getQueryCode, HospitalQcLedgerQuery::getQueryName)
            .eq(HospitalQcLedgerQuery::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .orderByAsc(HospitalQcLedgerQuery::getQueryCode)
            .orderByDesc(HospitalQcLedgerQuery::getId));
        Map<String, String> uniqueMap = new LinkedHashMap<>();
        for (HospitalQcLedgerQuery item : list) {
            uniqueMap.putIfAbsent(item.getQueryCode(), item.getQueryName());
        }
        List<HospitalQcOptionVo> options = new ArrayList<>();
        options.add(new HospitalQcOptionVo(HospitalQcConstants.QUERY_CODE_NONE, "无"));
        uniqueMap.forEach((code, name) -> options.add(new HospitalQcOptionVo(code, name + " (" + code + ")")));
        return options;
    }

    @Override
    public Boolean insertByBo(HospitalQcLedgerQueryBo bo) {
        validateBeforeSave(bo, null);
        HospitalQcLedgerQuery add = MapstructUtils.convert(bo, HospitalQcLedgerQuery.class);
        add.setStatus(bo.getStatus() == null ? HospitalQcConstants.STATUS_NORMAL : bo.getStatus());
        add.setIsDeleted(HospitalQcConstants.LOGIC_NOT_DELETED);
        boolean success = baseMapper.insert(add) > 0;
        if (success) {
            bo.setId(add.getId());
        }
        return success;
    }

    @Override
    public Boolean updateByBo(HospitalQcLedgerQueryBo bo) {
        HospitalQcLedgerQuery old = getEntity(bo.getId());
        validateBeforeSave(bo, bo.getId());
        HospitalQcLedgerQuery update = new HospitalQcLedgerQuery();
        update.setId(old.getId());
        update.setDeptId(bo.getDeptId());
        update.setDeptName(bo.getDeptName());
        update.setQueryCode(bo.getQueryCode());
        update.setQueryName(bo.getQueryName());
        update.setDatasourceId(bo.getDatasourceId());
        update.setCountSql(bo.getCountSql());
        update.setDetailSql(bo.getDetailSql());
        update.setDetailFieldMapping(bo.getDetailFieldMapping());
        update.setRemark(bo.getRemark());
        update.setStatus(bo.getStatus() == null ? old.getStatus() : bo.getStatus());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return baseMapper.update(new HospitalQcLedgerQuery(), new LambdaUpdateWrapper<HospitalQcLedgerQuery>()
            .set(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_DELETED)
            .in(HospitalQcLedgerQuery::getId, ids)
            .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)) > 0;
    }

    @Override
    public HospitalQcSqlTestResultVo testSql(HospitalQcLedgerQueryTestBo bo) {
        if (bo.getDatasourceId() == null) {
            throw new ServiceException("请先选择数据源");
        }
        HospitalQcSqlSecurityUtils.validateCountSql(bo.getCountSql(), "计数SQL");
        HospitalQcSqlSecurityUtils.validateDetailSql(bo.getDetailSql(), "明细SQL");

        HospitalQcDataSource dataSource = getActiveDataSource(bo.getDatasourceId());
        String plainPassword = passwordCrypto.decrypt(dataSource.getPasswordCipher());

        Date startTime = bo.getStartTime();
        Date endTime = bo.getEndTime();
        if (startTime == null || endTime == null) {
            String quarter = LocalDate.now().getYear() + "-Q" + ((LocalDate.now().getMonthValue() - 1) / 3 + 1);
            HospitalQcTimeRangeUtils.DateRange range = HospitalQcTimeRangeUtils.parseQuarterRange(quarter);
            startTime = range.startTime();
            endTime = range.endTime();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("deptId", bo.getDeptId());
        params.put("deptName", bo.getDeptName());

        long begin = System.currentTimeMillis();
        HospitalQcSqlTestResultVo result = new HospitalQcSqlTestResultVo();
        try {
            int timeout = resolveSqlTimeout();
            int rowLimit = resolveSqlTestLimit();
            HospitalQcSqlTestResultVo payload = jdbcExecutor.executeInSession(dataSource, plainPassword, timeout, session -> {
                HospitalQcSqlTestResultVo vo = new HospitalQcSqlTestResultVo();
                vo.setCountValue(jdbcExecutor.queryForCount(session, bo.getCountSql(), params));
                List<Map<String, Object>> rows = jdbcExecutor.queryForRows(session, bo.getDetailSql(), params, rowLimit);
                vo.setSampleRows(rows);
                // Always derive columns from metadata so they're populated even when 0 rows are returned
                List<String> columns = jdbcExecutor.queryForColumnNames(session, bo.getDetailSql(), params);
                vo.setColumns(columns);
                return vo;
            });
            result.setSuccess(true);
            result.setElapsedMs(System.currentTimeMillis() - begin);
            result.setCountValue(payload.getCountValue());
            result.setColumns(payload.getColumns());
            result.setSampleRows(payload.getSampleRows());
            result.setMessage("SQL 测试成功");
            return result;
        } catch (Exception ex) {
            result.setSuccess(false);
            result.setElapsedMs(System.currentTimeMillis() - begin);
            result.setMessage(ex.getMessage());
            result.setCountValue(0L);
            result.setColumns(List.of());
            result.setSampleRows(List.of());
            return result;
        }
    }

    private void validateBeforeSave(HospitalQcLedgerQueryBo bo, Long excludeId) {
        HospitalQcSqlSecurityUtils.validateCountSql(bo.getCountSql(), "计数SQL");
        HospitalQcSqlSecurityUtils.validateDetailSql(bo.getDetailSql(), "明细SQL");
        getActiveDataSource(bo.getDatasourceId());
        checkQueryCodeUnique(bo.getQueryCode(), bo.getDeptId(), excludeId);
    }

    private void checkQueryCodeUnique(String queryCode, Long deptId, Long excludeId) {
        LambdaQueryWrapper<HospitalQcLedgerQuery> lqw = Wrappers.lambdaQuery();
        lqw.select(HospitalQcLedgerQuery::getId);
        lqw.eq(HospitalQcLedgerQuery::getQueryCode, queryCode);
        lqw.eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED);
        if (deptId == null) {
            lqw.isNull(HospitalQcLedgerQuery::getDeptId);
        } else {
            lqw.eq(HospitalQcLedgerQuery::getDeptId, deptId);
        }
        lqw.ne(excludeId != null, HospitalQcLedgerQuery::getId, excludeId);
        lqw.last("limit 1");
        if (baseMapper.selectOne(lqw) != null) {
            throw new ServiceException("同科室下查询编码已存在");
        }
    }

    private HospitalQcLedgerQuery getEntity(Long id) {
        HospitalQcLedgerQuery entity = baseMapper.selectOne(Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
            .eq(HospitalQcLedgerQuery::getId, id)
            .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .last("limit 1"));
        if (entity == null) {
            throw new ServiceException("查询配置不存在");
        }
        return entity;
    }

    private HospitalQcDataSource getActiveDataSource(Long datasourceId) {
        HospitalQcDataSource dataSource = dataSourceMapper.selectOne(Wrappers.<HospitalQcDataSource>lambdaQuery()
            .eq(HospitalQcDataSource::getId, datasourceId)
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .eq(HospitalQcDataSource::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .last("limit 1"));
        if (dataSource == null) {
            throw new ServiceException("数据源不存在或已停用");
        }
        if (StringUtils.isBlank(dataSource.getPasswordCipher())) {
            throw new ServiceException("数据源未配置密码");
        }
        return dataSource;
    }

    private LambdaQueryWrapper<HospitalQcLedgerQuery> buildQueryWrapper(HospitalQcLedgerQueryBo bo) {
        LambdaQueryWrapper<HospitalQcLedgerQuery> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, HospitalQcLedgerQuery::getId, bo.getId());
        lqw.eq(bo.getDeptId() != null, HospitalQcLedgerQuery::getDeptId, bo.getDeptId());
        lqw.like(StringUtils.isNotBlank(bo.getDeptName()), HospitalQcLedgerQuery::getDeptName, bo.getDeptName());
        lqw.like(StringUtils.isNotBlank(bo.getQueryCode()), HospitalQcLedgerQuery::getQueryCode, bo.getQueryCode());
        lqw.like(StringUtils.isNotBlank(bo.getQueryName()), HospitalQcLedgerQuery::getQueryName, bo.getQueryName());
        lqw.eq(bo.getDatasourceId() != null, HospitalQcLedgerQuery::getDatasourceId, bo.getDatasourceId());
        lqw.eq(bo.getStatus() != null, HospitalQcLedgerQuery::getStatus, bo.getStatus());
        lqw.eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED);
        lqw.orderByAsc(HospitalQcLedgerQuery::getQueryCode);
        lqw.orderByDesc(HospitalQcLedgerQuery::getId);
        return lqw;
    }

    private int resolveSqlTimeout() {
        Integer timeout = properties.getSqlTimeoutSeconds();
        return timeout == null || timeout <= 0 ? HospitalQcConstants.DEFAULT_SQL_TIMEOUT_SECONDS : timeout;
    }

    private int resolveSqlTestLimit() {
        Integer limit = properties.getSqlTestLimit();
        return limit == null || limit <= 0 ? HospitalQcConstants.DEFAULT_SQL_TEST_LIMIT : limit;
    }
}

