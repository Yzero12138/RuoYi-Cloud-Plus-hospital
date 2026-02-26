package org.dromara.datacenter.nephrology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.nephrology.domain.LabReport;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Lab report view object.
 */
@Data
@AutoMapper(target = LabReport.class)
public class LabReportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String inpatientNo;
    private String outpatientNo;
    private Date reportTime;
    private String name;
    private String applyDept;
    private String reportName;
    private String idcardMask;
    private String idcardHash;
    private String sourceReportNo;
    private Date sourceUpdatedAt;
    private Date createdAt;
    private Date updatedAt;
}
