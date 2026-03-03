package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Dashboard/report filter options.
 */
@Data
public class HospitalQcDashboardFilterVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<HospitalQcOptionVo> deptOptions;

    private List<HospitalQcOptionVo> ledgerOptions;
}

