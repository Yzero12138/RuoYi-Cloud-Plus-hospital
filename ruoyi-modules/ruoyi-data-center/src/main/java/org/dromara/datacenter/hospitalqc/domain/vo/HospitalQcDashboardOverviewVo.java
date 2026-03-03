package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Dashboard overview response.
 */
@Data
public class HospitalQcDashboardOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer year;

    private List<String> quarters;

    private HospitalQcDashboardKpiVo kpi;

    private List<HospitalQcTrendSeriesVo> trends;

    private List<HospitalQcDeptRankingVo> rankings;

    private String noDenominatorRule;
}

