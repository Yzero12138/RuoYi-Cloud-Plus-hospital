package org.dromara.datacenter.nephrology.service.impl;

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
import org.dromara.datacenter.nephrology.domain.NephrologyLedgerQuery;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerQueryBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerQueryVo;
import org.dromara.datacenter.nephrology.mapper.NephrologyLedgerQueryMapper;
import org.dromara.datacenter.nephrology.service.INephrologyLedgerQueryService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Ledger query configuration service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class NephrologyLedgerQueryServiceImpl implements INephrologyLedgerQueryService {

    private static final Pattern FORBIDDEN_SQL = Pattern.compile(
        "\\b(insert|update|delete|drop|alter|truncate|create|replace)\\b",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern LIMIT_SQL = Pattern.compile("\\blimit\\b", Pattern.CASE_INSENSITIVE);

    private final NephrologyLedgerQueryMapper baseMapper;

    @Override
    public TableDataInfo<NephrologyLedgerQueryVo> selectPageList(NephrologyLedgerQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<NephrologyLedgerQuery> lqw = buildQueryWrapper(bo);
        Page<NephrologyLedgerQueryVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public List<NephrologyLedgerQueryVo> selectList(NephrologyLedgerQueryBo bo) {
        LambdaQueryWrapper<NephrologyLedgerQuery> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public List<NephrologyLedgerQueryVo> selectOptions() {
        LambdaQueryWrapper<NephrologyLedgerQuery> lqw = Wrappers.lambdaQuery();
        lqw.select(NephrologyLedgerQuery::getId, NephrologyLedgerQuery::getQueryCode, NephrologyLedgerQuery::getQueryName);
        lqw.eq(NephrologyLedgerQuery::getIsDeleted, 0);
        lqw.eq(NephrologyLedgerQuery::getStatus, 0);
        lqw.orderByAsc(NephrologyLedgerQuery::getQueryCode);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public NephrologyLedgerQueryVo selectById(Long id) {
        return baseMapper.selectVoOne(Wrappers.<NephrologyLedgerQuery>lambdaQuery()
            .eq(NephrologyLedgerQuery::getId, id)
            .eq(NephrologyLedgerQuery::getIsDeleted, 0));
    }

    @Override
    public Boolean insertByBo(NephrologyLedgerQueryBo bo) {
        validateSql(bo.getCountSql(), "计数SQL");
        validateSql(bo.getDetailSql(), "明细SQL");
        NephrologyLedgerQuery add = MapstructUtils.convert(bo, NephrologyLedgerQuery.class);
        if (add.getStatus() == null) {
            add.setStatus(0);
        }
        add.setIsDeleted(0);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(NephrologyLedgerQueryBo bo) {
        validateSql(bo.getCountSql(), "计数SQL");
        validateSql(bo.getDetailSql(), "明细SQL");
        NephrologyLedgerQuery update = MapstructUtils.convert(bo, NephrologyLedgerQuery.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return baseMapper.update(new NephrologyLedgerQuery(), new LambdaUpdateWrapper<NephrologyLedgerQuery>()
            .set(NephrologyLedgerQuery::getIsDeleted, 1)
            .in(NephrologyLedgerQuery::getId, ids)
            .eq(NephrologyLedgerQuery::getIsDeleted, 0)) > 0;
    }

    private LambdaQueryWrapper<NephrologyLedgerQuery> buildQueryWrapper(NephrologyLedgerQueryBo bo) {
        LambdaQueryWrapper<NephrologyLedgerQuery> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, NephrologyLedgerQuery::getId, bo.getId());
        lqw.eq(bo.getDeptId() != null, NephrologyLedgerQuery::getDeptId, bo.getDeptId());
        lqw.like(StringUtils.isNotBlank(bo.getDeptName()), NephrologyLedgerQuery::getDeptName, bo.getDeptName());
        lqw.like(StringUtils.isNotBlank(bo.getQueryCode()), NephrologyLedgerQuery::getQueryCode, bo.getQueryCode());
        lqw.like(StringUtils.isNotBlank(bo.getQueryName()), NephrologyLedgerQuery::getQueryName, bo.getQueryName());
        lqw.eq(bo.getStatus() != null, NephrologyLedgerQuery::getStatus, bo.getStatus());
        lqw.eq(NephrologyLedgerQuery::getIsDeleted, 0);
        lqw.orderByAsc(NephrologyLedgerQuery::getQueryCode);
        lqw.orderByDesc(NephrologyLedgerQuery::getId);
        return lqw;
    }

    private void validateSql(String sql, String label) {
        if (StringUtils.isBlank(sql)) {
            throw new ServiceException(label + "不能为空");
        }
        String normalized = sql.trim().toLowerCase(Locale.ROOT);
        if (!(normalized.startsWith("select") || normalized.startsWith("with"))) {
            throw new ServiceException(label + "仅支持SELECT语句");
        }
        if (normalized.contains(";")) {
            throw new ServiceException(label + "不允许包含分号");
        }
        if (FORBIDDEN_SQL.matcher(normalized).find()) {
            throw new ServiceException(label + "包含不允许的关键字");
        }
        if (label.contains("明细") && LIMIT_SQL.matcher(normalized).find()) {
            throw new ServiceException(label + "不允许包含LIMIT，请交由系统分页");
        }
        if (!sql.contains(":startTime") || !sql.contains(":endTime")) {
            throw new ServiceException(label + "必须包含:startTime和:endTime时间条件");
        }
    }
}

