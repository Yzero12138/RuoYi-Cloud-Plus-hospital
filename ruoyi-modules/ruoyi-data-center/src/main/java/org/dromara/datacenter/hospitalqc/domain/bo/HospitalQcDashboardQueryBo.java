package org.dromara.datacenter.hospitalqc.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * Dashboard query condition.
 */
@Data
public class HospitalQcDashboardQueryBo {

    private Integer year;

    private List<Long> deptIds;

    private List<String> ledgerCodes;
}

