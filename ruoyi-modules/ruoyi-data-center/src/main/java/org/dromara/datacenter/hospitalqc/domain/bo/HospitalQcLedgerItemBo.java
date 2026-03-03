package org.dromara.datacenter.hospitalqc.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerItem;

/**
 * Ledger tree node create/update request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HospitalQcLedgerItem.class, reverseConvertGenerate = false)
public class HospitalQcLedgerItemBo extends BaseEntity {

    @NotNull(message = "ID不能为空", groups = {EditGroup.class})
    private Long id;

    private Long parentId;

    @NotBlank(message = "台账编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ledgerCode;

    @NotBlank(message = "台账名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String ledgerName;

    @NotBlank(message = "节点类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String nodeType;

    private String queryCode;

    private Integer sortOrder;

    private Integer status;

    private String remark;
}

