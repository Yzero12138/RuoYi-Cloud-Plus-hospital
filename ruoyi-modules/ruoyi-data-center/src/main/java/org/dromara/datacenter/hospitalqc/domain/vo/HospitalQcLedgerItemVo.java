package org.dromara.datacenter.hospitalqc.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerItem;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Ledger tree node response.
 */
@Data
@AutoMapper(target = HospitalQcLedgerItem.class)
public class HospitalQcLedgerItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String ledgerCode;
    private String ledgerName;
    private String nodeType;
    private String queryCode;
    private Long deptId;
    private String deptName;
    private Integer sortOrder;
    private Integer status;
    private String remark;

    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;

    private List<HospitalQcLedgerItemVo> children;
}

