package org.dromara.datacenter.nephrology.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.HdPatient;
import org.dromara.datacenter.nephrology.domain.bo.HdPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.HdPatientVo;
import org.dromara.datacenter.nephrology.mapper.HdPatientMapper;
import org.dromara.datacenter.nephrology.service.IHdPatientService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Hemodialysis patient service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class HdPatientServiceImpl implements IHdPatientService {

    private final HdPatientMapper baseMapper;

    @Override
    public TableDataInfo<HdPatientVo> selectPageHdPatientList(HdPatientBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HdPatient> lqw = buildQueryWrapper(bo);
        Page<HdPatientVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public HdPatientVo selectHdPatientById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public List<HdPatientVo> selectHdPatientList(HdPatientBo bo) {
        LambdaQueryWrapper<HdPatient> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<HdPatient> buildQueryWrapper(HdPatientBo bo) {
        LambdaQueryWrapper<HdPatient> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, HdPatient::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getName()), HdPatient::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getGender()), HdPatient::getGender, bo.getGender());
        lqw.eq(StringUtils.isNotBlank(bo.getInpatientNo()), HdPatient::getInpatientNo, bo.getInpatientNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOutpatientNo()), HdPatient::getOutpatientNo, bo.getOutpatientNo());
        lqw.eq(StringUtils.isNotBlank(bo.getIdcardHash()), HdPatient::getIdcardHash, bo.getIdcardHash());
        lqw.eq(StringUtils.isNotBlank(bo.getSourcePatientId()), HdPatient::getSourcePatientId, bo.getSourcePatientId());
        lqw.orderByDesc(HdPatient::getId);
        return lqw;
    }
}
