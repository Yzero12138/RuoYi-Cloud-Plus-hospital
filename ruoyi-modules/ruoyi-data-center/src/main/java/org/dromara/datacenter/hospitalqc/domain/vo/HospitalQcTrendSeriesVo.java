package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Trend series for one ledger.
 */
@Data
public class HospitalQcTrendSeriesVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String ledgerCode;

    private String ledgerName;

    private List<HospitalQcQuarterMetricVo> quarterMetrics;
}

