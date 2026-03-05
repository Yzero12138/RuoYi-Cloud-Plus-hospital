package org.dromara.datacenter.hospitalqc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Ledger tree item entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hospital_qc_ledger_item")
public class HospitalQcLedgerItem extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private String ledgerCode;

    private String ledgerName;

    /**
     * I indicator, N numerator, D denominator.
     */
    private String nodeType;

    /**
     * Bound query code from hospital_qc_ledger_query.
     */
    private String queryCode;

    private Long deptId;

    private String deptName;

    private Integer sortOrder;

    private Integer status;

    private Integer isDeleted;

    private String remark;
}

