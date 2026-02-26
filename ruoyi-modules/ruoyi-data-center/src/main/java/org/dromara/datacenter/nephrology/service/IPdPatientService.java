package org.dromara.datacenter.nephrology.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.PdPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.PdPatientVo;

import java.util.List;

/**
 * Peritoneal dialysis patient service.
 */
public interface IPdPatientService {

    TableDataInfo<PdPatientVo> selectPagePdPatientList(PdPatientBo bo, PageQuery pageQuery);

    PdPatientVo selectPdPatientById(Long id);

    List<PdPatientVo> selectPdPatientList(PdPatientBo bo);
}
