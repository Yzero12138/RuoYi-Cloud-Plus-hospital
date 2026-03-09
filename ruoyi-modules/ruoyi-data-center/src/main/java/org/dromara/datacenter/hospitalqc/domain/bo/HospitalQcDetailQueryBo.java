package org.dromara.datacenter.hospitalqc.domain.bo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Detail data query conditions.
 */
@Data
public class HospitalQcDetailQueryBo {

    /**
     * Ledger code (required).
     */
    private String ledgerCode;

    /**
     * Department IDs (optional, filtered by permission).
     */
    private List<Long> deptIds;

    /**
     * Start time (required).
     */
    private Date startTime;

    /**
     * End time (required).
     */
    private Date endTime;

    /**
     * Node type filter: N=numerator, D=denominator.
     * If null, returns the first available detail query.
     */
    private String nodeType;

    /**
     * Page number (1-based).
     */
    private Integer pageNum = 1;

    /**
     * Page size.
     */
    private Integer pageSize = 20;
}
