package org.dromara.datacenter.nephrology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.nephrology.domain.LabResult;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Lab result detail view object.
 */
@Data
@AutoMapper(target = LabResult.class)
public class LabResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sourceReportNo;
    private String itemCode;
    private String itemName;
    private String resultValue;
    private String resultUnit;
    private String refRange;
    private String abnormalFlag;
    private Date createdAt;
}
