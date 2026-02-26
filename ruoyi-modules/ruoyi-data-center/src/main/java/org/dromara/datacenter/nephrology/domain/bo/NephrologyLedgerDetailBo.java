package org.dromara.datacenter.nephrology.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Ledger detail query bo.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NephrologyLedgerDetailBo extends BaseEntity {

    private String ledgerCode;

    private String dischargeTimeType;

    private String dischargeTimeValue;

    private String medicalRecordNo;

    private String idCardNo;
}
