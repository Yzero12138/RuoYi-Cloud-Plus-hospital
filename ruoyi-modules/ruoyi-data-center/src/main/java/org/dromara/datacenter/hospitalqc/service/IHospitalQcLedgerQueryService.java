package org.dromara.datacenter.hospitalqc.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerQueryBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerQueryTestBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcLedgerQueryVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcOptionVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcSqlTestResultVo;

import java.util.Collection;
import java.util.List;

/**
 * Ledger SQL config service.
 */
public interface IHospitalQcLedgerQueryService {

    TableDataInfo<HospitalQcLedgerQueryVo> selectPageList(HospitalQcLedgerQueryBo bo, PageQuery pageQuery);

    HospitalQcLedgerQueryVo selectById(Long id);

    List<HospitalQcOptionVo> selectOptions();

    Boolean insertByBo(HospitalQcLedgerQueryBo bo);

    Boolean updateByBo(HospitalQcLedgerQueryBo bo);

    Boolean deleteByIds(Collection<Long> ids);

    HospitalQcSqlTestResultVo testSql(HospitalQcLedgerQueryTestBo bo);
}

