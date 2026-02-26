package org.dromara.datacenter.nephrology.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.datacenter.nephrology.domain.LabResult;

/**
 * Lab result detail query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = LabResult.class, reverseConvertGenerate = false)
public class LabResultBo extends BaseEntity {

    private Long id;
    private Long sourceReportNo;
    private String itemCode;
    private String itemName;
    private String abnormalFlag;
}
