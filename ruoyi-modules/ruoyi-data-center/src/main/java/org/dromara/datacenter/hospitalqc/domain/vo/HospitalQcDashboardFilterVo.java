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

    /**
     * Whether current user is admin.
     */
    private Boolean isAdmin;

    /**
     * Current user's department ID.
     */
    private Long currentDeptId;

    /**
     * Current user's department name.
     */
    private String currentDeptName;

    /**
     * List of department IDs that current user can access.
     * Empty list means all departments (admin).
     */
    private List<Long> allowedDeptIds;

    private List<HospitalQcOptionVo> deptOptions;

    private List<HospitalQcOptionVo> ledgerOptions;
}
