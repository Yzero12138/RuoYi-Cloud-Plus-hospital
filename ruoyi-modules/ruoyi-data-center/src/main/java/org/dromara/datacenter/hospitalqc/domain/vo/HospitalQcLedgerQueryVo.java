package org.dromara.datacenter.hospitalqc.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerQuery;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Ledger SQL query config response.
 */
@Data
@AutoMapper(target = HospitalQcLedgerQuery.class)
public class HospitalQcLedgerQueryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String deptName;
    private String queryCode;
    private String queryName;
    private Long datasourceId;
    private String countSql;
    private String detailSql;
    private String detailFieldMapping;
    private Integer status;
    private String remark;

    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
}

