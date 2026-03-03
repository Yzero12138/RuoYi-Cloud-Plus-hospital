package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * KPI card values for dashboard.
 */
@Data
public class HospitalQcDashboardKpiVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long numerator;

    private Long denominator;

    private BigDecimal indicatorPercent;

    private BigDecimal qoqGrowth;

    private BigDecimal percentagePointChange;

    private String percentDisplay;

    private String growthDisplay;

    private String pointChangeDisplay;

    private Boolean noDenominator;
}

