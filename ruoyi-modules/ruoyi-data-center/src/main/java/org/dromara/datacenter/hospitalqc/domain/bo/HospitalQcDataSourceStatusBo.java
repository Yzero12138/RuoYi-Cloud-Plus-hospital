package org.dromara.datacenter.hospitalqc.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Datasource status change request.
 */
@Data
public class HospitalQcDataSourceStatusBo {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}

