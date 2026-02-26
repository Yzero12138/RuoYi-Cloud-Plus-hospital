package org.dromara.datacenter.nephrology.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.datacenter.nephrology.domain.LabReport;

/**
 * Lab report query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = LabReport.class, reverseConvertGenerate = false)
public class LabReportBo extends BaseEntity {

    private Long id;
    private String inpatientNo;
    private String outpatientNo;
    private String name;
    private String reportName;
    private String idcardHash;
    private String sourceReportNo;
}
