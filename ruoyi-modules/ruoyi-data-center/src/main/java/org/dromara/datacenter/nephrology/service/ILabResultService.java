package org.dromara.datacenter.nephrology.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.LabResultBo;
import org.dromara.datacenter.nephrology.domain.vo.LabResultVo;

import java.util.List;

/**
 * Lab result detail service.
 */
public interface ILabResultService {

    TableDataInfo<LabResultVo> selectPageLabResultList(LabResultBo bo, PageQuery pageQuery);

    LabResultVo selectLabResultById(Long id);

    List<LabResultVo> selectLabResultList(LabResultBo bo);
}
