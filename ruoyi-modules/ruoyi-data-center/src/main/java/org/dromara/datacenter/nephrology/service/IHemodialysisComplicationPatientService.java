package org.dromara.datacenter.nephrology.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.HemodialysisComplicationPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.HemodialysisComplicationPatientVo;

import java.util.List;

/**
 * Hemodialysis complication patient service.
 */
public interface IHemodialysisComplicationPatientService {

    TableDataInfo<HemodialysisComplicationPatientVo> selectPageList(HemodialysisComplicationPatientBo bo, PageQuery pageQuery);

    HemodialysisComplicationPatientVo selectById(Long id);

    List<HemodialysisComplicationPatientVo> selectList(HemodialysisComplicationPatientBo bo);
}
