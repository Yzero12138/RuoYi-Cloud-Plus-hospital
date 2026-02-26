package org.dromara.datacenter.nephrology.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerCountBo;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerDetailBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerCountVo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerDetailVo;

import java.util.List;

/**
 * Ledger query service.
 */
public interface INephrologyLedgerService {

    List<NephrologyLedgerCountVo> selectCountTree(NephrologyLedgerCountBo bo);

    TableDataInfo<NephrologyLedgerDetailVo> selectDetailPage(NephrologyLedgerDetailBo bo, PageQuery pageQuery);
}
