package org.dromara.datacenter.nephrology.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.LabResult;
import org.dromara.datacenter.nephrology.domain.bo.LabResultBo;
import org.dromara.datacenter.nephrology.domain.vo.LabResultVo;
import org.dromara.datacenter.nephrology.mapper.LabResultMapper;
import org.dromara.datacenter.nephrology.service.ILabResultService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lab result detail service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class LabResultServiceImpl implements ILabResultService {

    private final LabResultMapper baseMapper;

    @Override
    public TableDataInfo<LabResultVo> selectPageLabResultList(LabResultBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<LabResult> lqw = buildQueryWrapper(bo);
        Page<LabResultVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public LabResultVo selectLabResultById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public List<LabResultVo> selectLabResultList(LabResultBo bo) {
        LambdaQueryWrapper<LabResult> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<LabResult> buildQueryWrapper(LabResultBo bo) {
        LambdaQueryWrapper<LabResult> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, LabResult::getId, bo.getId());
        lqw.eq(bo.getSourceReportNo() != null, LabResult::getSourceReportNo, bo.getSourceReportNo());
        lqw.eq(StringUtils.isNotBlank(bo.getItemCode()), LabResult::getItemCode, bo.getItemCode());
        lqw.like(StringUtils.isNotBlank(bo.getItemName()), LabResult::getItemName, bo.getItemName());
        lqw.eq(StringUtils.isNotBlank(bo.getAbnormalFlag()), LabResult::getAbnormalFlag, bo.getAbnormalFlag());
        lqw.orderByDesc(LabResult::getId);
        return lqw;
    }
}
