package org.dromara.datacenter.nephrology.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.PdPatient;
import org.dromara.datacenter.nephrology.domain.bo.PdPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.PdPatientVo;
import org.dromara.datacenter.nephrology.mapper.PdPatientMapper;
import org.dromara.datacenter.nephrology.service.IPdPatientService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Peritoneal dialysis patient service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class PdPatientServiceImpl implements IPdPatientService {

    private final PdPatientMapper baseMapper;

    @Override
    public TableDataInfo<PdPatientVo> selectPagePdPatientList(PdPatientBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PdPatient> lqw = buildQueryWrapper(bo);
        Page<PdPatientVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public PdPatientVo selectPdPatientById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public List<PdPatientVo> selectPdPatientList(PdPatientBo bo) {
        LambdaQueryWrapper<PdPatient> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<PdPatient> buildQueryWrapper(PdPatientBo bo) {
        LambdaQueryWrapper<PdPatient> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, PdPatient::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getName()), PdPatient::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getGender()), PdPatient::getGender, bo.getGender());
        lqw.eq(StringUtils.isNotBlank(bo.getInpatientNo()), PdPatient::getInpatientNo, bo.getInpatientNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOutpatientNo()), PdPatient::getOutpatientNo, bo.getOutpatientNo());
        lqw.eq(StringUtils.isNotBlank(bo.getIdcardHash()), PdPatient::getIdcardHash, bo.getIdcardHash());
        lqw.eq(StringUtils.isNotBlank(bo.getSourcePatientId()), PdPatient::getSourcePatientId, bo.getSourcePatientId());
        lqw.orderByDesc(PdPatient::getId);
        return lqw;
    }
}
