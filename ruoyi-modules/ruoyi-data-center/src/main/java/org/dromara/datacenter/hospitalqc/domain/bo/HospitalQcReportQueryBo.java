package org.dromara.datacenter.hospitalqc.domain.bo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Report query condition.
 */
@Data
public class HospitalQcReportQueryBo {

    /**
     * quarter/month/custom
     */
    private String timeType;

    /**
     * quarter: 2026-Q1, month: 2026-01
     */
    private String timeValue;

    private Date startTime;

    private Date endTime;

    private List<Long> deptIds;

    private List<String> ledgerCodes;
}

