package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Dept ranking row on latest quarter.
 */
@Data
public class HospitalQcDeptRankingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long deptId;

    private String deptName;

    private Long numerator;

    private Long denominator;

    private BigDecimal indicatorPercent;

    private BigDecimal qoqGrowth;

    private BigDecimal percentagePointChange;

    private String trend;

    private String percentDisplay;

    private String growthDisplay;

    private String pointChangeDisplay;
}

