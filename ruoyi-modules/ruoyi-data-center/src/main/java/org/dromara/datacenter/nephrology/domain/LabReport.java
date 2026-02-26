package org.dromara.datacenter.nephrology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Lab report table lab_report.
 */
@Data
@TableName("lab_report")
public class LabReport {

    @TableId(value = "id", type = IdType.AUTO)
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
