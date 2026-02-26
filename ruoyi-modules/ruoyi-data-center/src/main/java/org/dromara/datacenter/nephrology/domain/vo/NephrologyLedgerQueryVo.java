package org.dromara.datacenter.nephrology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.nephrology.domain.NephrologyLedgerQuery;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Ledger query configuration vo.
 */
@Data
@AutoMapper(target = NephrologyLedgerQuery.class)
public class NephrologyLedgerQueryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long deptId;
    private String deptName;
    private String queryCode;
    private String queryName;
    private String countSql;
    private String detailSql;
    private Integer status;
    private String remark;

    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
}

