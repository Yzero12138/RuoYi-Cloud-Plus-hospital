package org.dromara.datacenter.nephrology.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.HdPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.HdPatientVo;

import java.util.List;

/**
 * Hemodialysis patient service.
 */
public interface IHdPatientService {

    TableDataInfo<HdPatientVo> selectPageHdPatientList(HdPatientBo bo, PageQuery pageQuery);

    HdPatientVo selectHdPatientById(Long id);

    List<HdPatientVo> selectHdPatientList(HdPatientBo bo);
}
