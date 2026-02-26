package org.dromara.datacenter.nephrology.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.datacenter.nephrology.domain.NephrologyLedgerItem;

/**
 * Ledger item maintenance bo.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = NephrologyLedgerItem.class, reverseConvertGenerate = false)
public class NephrologyLedgerItemBo extends BaseEntity {

    @NotNull(message = "ID不能为空", groups = {EditGroup.class})
    private Long id;

    private Long parentId;

    @NotBlank(message = "台账编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ledgerCode;

    @NotBlank(message = "台账名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ledgerName;

    @NotBlank(message = "节点类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String nodeType;

    @NotBlank(message = "查询目标不能为空", groups = {AddGroup.class, EditGroup.class})
    private String queryTarget;

    private Integer sortOrder;
    private Integer status;
    private String remark;
}
