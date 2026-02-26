package org.dromara.datacenter.nephrology.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Ledger tree row with count values.
 */
@Data
public class NephrologyLedgerCountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String ledgerCode;
    private String ledgerName;
    private String nodeType;
    private String queryTarget;
    private Integer sortOrder;

    /**
     * Leaf count value.
     */
    private Long countValue;

    /**
     * Summary count value on parent rows.
     */
    private Long numeratorCount;

    /**
     * Summary count value on parent rows.
     */
    private Long denominatorCount;

    /**
     * Leaf row can jump to detail.
     */
    private Boolean queryable;

    private List<NephrologyLedgerCountVo> children;
}
