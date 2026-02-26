package org.dromara.datacenter.nephrology.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.LabReport;
import org.dromara.datacenter.nephrology.domain.bo.LabReportBo;
import org.dromara.datacenter.nephrology.domain.vo.LabReportVo;
import org.dromara.datacenter.nephrology.mapper.LabReportMapper;
import org.dromara.datacenter.nephrology.service.ILabReportService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lab report service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class LabReportServiceImpl implements ILabReportService {

    private final LabReportMapper baseMapper;

    @Override
    public TableDataInfo<LabReportVo> selectPageLabReportList(LabReportBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<LabReport> lqw = buildQueryWrapper(bo);
        Page<LabReportVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public LabReportVo selectLabReportById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public List<LabReportVo> selectLabReportList(LabReportBo bo) {
        LambdaQueryWrapper<LabReport> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<LabReport> buildQueryWrapper(LabReportBo bo) {
        LambdaQueryWrapper<LabReport> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, LabReport::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getName()), LabReport::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getInpatientNo()), LabReport::getInpatientNo, bo.getInpatientNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOutpatientNo()), LabReport::getOutpatientNo, bo.getOutpatientNo());
        lqw.like(StringUtils.isNotBlank(bo.getReportName()), LabReport::getReportName, bo.getReportName());
        lqw.eq(StringUtils.isNotBlank(bo.getIdcardHash()), LabReport::getIdcardHash, bo.getIdcardHash());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceReportNo()), LabReport::getSourceReportNo, bo.getSourceReportNo());
        lqw.orderByDesc(LabReport::getId);
        return lqw;
    }
}
