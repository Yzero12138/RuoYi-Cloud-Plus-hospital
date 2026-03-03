package org.dromara.datacenter.hospitalqc.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceStatusBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceTestBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcConnectionTestVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDataSourceVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcOptionVo;

import java.util.Collection;
import java.util.List;

/**
 * Datasource config service.
 */
public interface IHospitalQcDataSourceService {

    TableDataInfo<HospitalQcDataSourceVo> selectPageList(HospitalQcDataSourceBo bo, PageQuery pageQuery);

    HospitalQcDataSourceVo selectById(Long id);

    List<HospitalQcOptionVo> selectEnabledOptions();

    Boolean insertByBo(HospitalQcDataSourceBo bo);

    Boolean updateByBo(HospitalQcDataSourceBo bo);

    Boolean updateStatus(HospitalQcDataSourceStatusBo bo);

    Boolean deleteByIds(Collection<Long> ids);

    HospitalQcConnectionTestVo testConnection(HospitalQcDataSourceTestBo bo);
}

