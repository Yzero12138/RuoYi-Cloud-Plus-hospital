package org.dromara.datacenter.nephrology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Lab result detail table lab_result.
 */
@Data
@TableName("lab_result")
public class LabResult {

    @TableId(value = "id", type = IdType.AUTO)
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
