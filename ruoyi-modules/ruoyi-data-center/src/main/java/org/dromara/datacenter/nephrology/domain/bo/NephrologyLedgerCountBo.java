package org.dromara.datacenter.nephrology.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Ledger count query bo.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NephrologyLedgerCountBo extends BaseEntity {

    /**
     * year / quarter / month
     */
    private String dischargeTimeType;

    /**
     * year: 2026, quarter: 2026-Q1, month: 2026-01
     */
    private String dischargeTimeValue;
}
