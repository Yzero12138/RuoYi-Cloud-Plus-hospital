package org.dromara.datacenter.nephrology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Hemodialysis complication patient table.
 */
@Data
@TableName("hemodialysis_complication_patient")
public class HemodialysisComplicationPatient {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String medicalRecordNo;
    private String patientName;
    /**
     * 1 male, 2 female.
     */
    private Integer gender;
    private Date birthDate;
    private Integer age;
    private String idCardNo;
    private String admissionDepartment;
    private String dischargeDepartment;
    private String complicationDiagnosis;
    private String complicationCode;
    /**
     * 1 present, 2 uncertain, 3 unknown, 4 none.
     */
    private Integer admissionCondition;
    private Date admissionTime;
    private Date dischargeTime;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
}
