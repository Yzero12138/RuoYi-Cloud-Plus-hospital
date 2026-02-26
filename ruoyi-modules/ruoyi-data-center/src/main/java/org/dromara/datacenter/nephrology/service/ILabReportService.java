package org.dromara.datacenter.nephrology.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.LabReportBo;
import org.dromara.datacenter.nephrology.domain.vo.LabReportVo;

import java.util.List;

/**
 * Lab report service.
 */
public interface ILabReportService {

    TableDataInfo<LabReportVo> selectPageLabReportList(LabReportBo bo, PageQuery pageQuery);

    LabReportVo selectLabReportById(Long id);

    List<LabReportVo> selectLabReportList(LabReportBo bo);
}
