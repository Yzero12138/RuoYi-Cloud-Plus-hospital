package org.dromara.datacenter.nephrology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Ledger detail row.
 */
@Data
public class NephrologyLedgerDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String medicalRecordNo;
    private String patientName;
    private String idCardNo;
    private String dischargeDepartment;
    private Date dischargeTime;
    private String diagnosisName;
    private String diagnosisCode;
    private String sourceType;
}
