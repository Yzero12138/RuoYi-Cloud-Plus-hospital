package org.dromara.datacenter.nephrology.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerQueryBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerQueryVo;

import java.util.Collection;
import java.util.List;

/**
 * Ledger query configuration service.
 */
public interface INephrologyLedgerQueryService {

    TableDataInfo<NephrologyLedgerQueryVo> selectPageList(NephrologyLedgerQueryBo bo, PageQuery pageQuery);

    List<NephrologyLedgerQueryVo> selectList(NephrologyLedgerQueryBo bo);

    List<NephrologyLedgerQueryVo> selectOptions();

    NephrologyLedgerQueryVo selectById(Long id);

    Boolean insertByBo(NephrologyLedgerQueryBo bo);

    Boolean updateByBo(NephrologyLedgerQueryBo bo);

    Boolean deleteByIds(Collection<Long> ids);
}

