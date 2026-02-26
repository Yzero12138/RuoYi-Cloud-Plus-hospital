package org.dromara.datacenter.nephrology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Ledger query configuration for nephrology.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nephrology_ledger_query")
public class NephrologyLedgerQuery extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long deptId;

    private String deptName;

    private String queryCode;

    private String queryName;

    private String countSql;

    private String detailSql;

    private Integer status;

    private Integer isDeleted;

    private String remark;
}

