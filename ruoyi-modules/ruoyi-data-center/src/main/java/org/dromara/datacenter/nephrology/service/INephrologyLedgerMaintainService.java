package org.dromara.datacenter.nephrology.service;

import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerItemBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerItemVo;

import java.util.Collection;
import java.util.List;

/**
 * Ledger maintenance service.
 */
public interface INephrologyLedgerMaintainService {

    List<NephrologyLedgerItemVo> selectTreeList(NephrologyLedgerItemBo bo);

    NephrologyLedgerItemVo selectById(Long id);

    Boolean insertByBo(NephrologyLedgerItemBo bo);

    Boolean updateByBo(NephrologyLedgerItemBo bo);

    Boolean deleteWithChildrenByIds(Collection<Long> ids);
}
