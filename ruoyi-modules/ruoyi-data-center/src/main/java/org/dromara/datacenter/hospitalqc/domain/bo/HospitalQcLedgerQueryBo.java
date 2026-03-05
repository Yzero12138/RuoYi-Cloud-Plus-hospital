package org.dromara.datacenter.hospitalqc.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerQuery;

/**
 * Ledger SQL query config create/update request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HospitalQcLedgerQuery.class, reverseConvertGenerate = false)
public class HospitalQcLedgerQueryBo extends BaseEntity {

    @NotNull(message = "ID不能为空", groups = {EditGroup.class})
    private Long id;

    private Long deptId;

    private String deptName;

    @NotBlank(message = "查询编码不能为空", groups = {AddGroup.class, EditGroup.class})
    private String queryCode;

    @NotBlank(message = "查询名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String queryName;

    @NotNull(message = "数据源不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long datasourceId;

    @NotBlank(message = "计数SQL不能为空", groups = {AddGroup.class, EditGroup.class})
    private String countSql;

    @NotBlank(message = "明细SQL不能为空", groups = {AddGroup.class, EditGroup.class})
    private String detailSql;

    private String detailFieldMapping;

    private Integer status;

    private String remark;
}

