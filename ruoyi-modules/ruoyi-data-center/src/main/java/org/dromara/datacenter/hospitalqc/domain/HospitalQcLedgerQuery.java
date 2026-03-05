package org.dromara.datacenter.hospitalqc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Ledger SQL query configuration.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hospital_qc_ledger_query")
public class HospitalQcLedgerQuery extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
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

    private Integer isDeleted;

    private String remark;
}

