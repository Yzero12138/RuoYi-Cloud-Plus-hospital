package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Report summary row by dept and ledger.
 */
@Data
public class HospitalQcReportRowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String periodLabel;

    private Long deptId;

    private String deptName;

    private String ledgerCode;

    private String ledgerName;

    private Long numerator;

    private Long denominator;

    private BigDecimal indicatorPercent;

    private BigDecimal qoqGrowth;

    private BigDecimal percentagePointChange;

    private Boolean noDenominator;

    private String percentDisplay;

    private String growthDisplay;

    private String pointChangeDisplay;
}

