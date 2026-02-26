package org.dromara.datacenter.nephrology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.nephrology.domain.NephrologyLedgerItem;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Ledger item maintenance vo.
 */
@Data
@AutoMapper(target = NephrologyLedgerItem.class)
public class NephrologyLedgerItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String ledgerCode;
    private String ledgerName;
    private String nodeType;
    private String queryTarget;
    private Integer sortOrder;
    private Integer status;
    private String remark;

    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;

    private List<NephrologyLedgerItemVo> children;
}
