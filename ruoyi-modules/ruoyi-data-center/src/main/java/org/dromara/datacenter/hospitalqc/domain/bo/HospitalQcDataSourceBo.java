package org.dromara.datacenter.hospitalqc.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Datasource create/update request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HospitalQcDataSourceBo extends BaseEntity {

    @NotNull(message = "ID不能为空", groups = {EditGroup.class})
    private Long id;

    @NotBlank(message = "数据源名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String sourceName;

    @NotBlank(message = "数据源类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String sourceType;

    @NotBlank(message = "主机不能为空", groups = {AddGroup.class, EditGroup.class})
    private String host;

    private Integer port;

    private String databaseName;

    private String instanceName;

    @NotBlank(message = "用户名不能为空", groups = {AddGroup.class, EditGroup.class})
    private String username;

    @NotBlank(message = "密码不能为空", groups = {AddGroup.class})
    private String password;

    private Integer status;

    private String remark;
}

