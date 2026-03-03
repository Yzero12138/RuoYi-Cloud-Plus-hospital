package org.dromara.datacenter.hospitalqc.domain.bo;

import lombok.Data;

import java.util.Date;

/**
 * SQL test request for query config.
 */
@Data
public class HospitalQcLedgerQueryTestBo {

    private Long datasourceId;

    private String countSql;

    private String detailSql;

    private Date startTime;

    private Date endTime;

    private Long deptId;

    private String deptName;
}

