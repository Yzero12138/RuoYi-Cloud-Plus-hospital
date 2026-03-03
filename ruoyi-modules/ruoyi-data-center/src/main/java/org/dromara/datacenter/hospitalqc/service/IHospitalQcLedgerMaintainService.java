package org.dromara.datacenter.hospitalqc.service;

import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerItemBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcLedgerItemVo;

import java.util.Collection;
import java.util.List;

/**
 * Ledger tree maintenance service.
 */
public interface IHospitalQcLedgerMaintainService {

    List<HospitalQcLedgerItemVo> selectTreeList(HospitalQcLedgerItemBo bo);

    HospitalQcLedgerItemVo selectById(Long id);

    Boolean insertByBo(HospitalQcLedgerItemBo bo);

    Boolean updateByBo(HospitalQcLedgerItemBo bo);

    Boolean deleteWithChildrenByIds(Collection<Long> ids);
}

