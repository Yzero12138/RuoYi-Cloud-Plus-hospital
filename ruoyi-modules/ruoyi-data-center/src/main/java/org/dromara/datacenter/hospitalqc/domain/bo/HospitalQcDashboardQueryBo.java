package org.dromara.datacenter.hospitalqc.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * Dashboard query condition.
 */
@Data
public class HospitalQcDashboardQueryBo {
    private Integer year;

    /**
     * Quarter window size (optional, uses config default if not provided).
     * Min 1, max 12.
     */
    private Integer quarterWindow;

    private List<Long> deptIds;

    private List<String> ledgerCodes;
}
