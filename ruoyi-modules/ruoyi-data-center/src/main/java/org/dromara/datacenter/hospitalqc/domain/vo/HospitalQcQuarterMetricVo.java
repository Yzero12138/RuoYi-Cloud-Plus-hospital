package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Quarter metric row.
 */
@Data
public class HospitalQcQuarterMetricVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String quarter;

    private Long numerator;

    private Long denominator;

    private BigDecimal indicatorPercent;

    /**
     * QoQ growth value, denominator>0 uses percent growth, otherwise numerator growth.
     */
    private BigDecimal qoqGrowth;

    /**
     * Percentage point change when denominator>0.
     */
    private BigDecimal percentagePointChange;

    private Boolean noDenominator;

    private String percentDisplay;

    private String growthDisplay;

    private String pointChangeDisplay;
}

