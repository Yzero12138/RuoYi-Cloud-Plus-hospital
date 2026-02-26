package org.dromara.datacenter.nephrology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.nephrology.domain.HemodialysisComplicationPatient;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Hemodialysis complication patient view object.
 */
@Data
@AutoMapper(target = HemodialysisComplicationPatient.class)
public class HemodialysisComplicationPatientVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String medicalRecordNo;
    private String patientName;
    private Integer gender;
    private Date birthDate;
    private Integer age;
    private String idCardNo;
    private String admissionDepartment;
    private String dischargeDepartment;
    private String complicationDiagnosis;
    private String complicationCode;
    private Integer admissionCondition;
    private Date admissionTime;
    private Date dischargeTime;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
}
