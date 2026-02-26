package org.dromara.datacenter.nephrology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Ledger maintenance config for nephrology indicators.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("nephrology_ledger_item")
public class NephrologyLedgerItem extends BaseEntity {

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
     * Query target enum.
     */
    private String queryTarget;

    private Integer sortOrder;

    private Integer status;

    private Integer isDeleted;

    private String remark;
}
