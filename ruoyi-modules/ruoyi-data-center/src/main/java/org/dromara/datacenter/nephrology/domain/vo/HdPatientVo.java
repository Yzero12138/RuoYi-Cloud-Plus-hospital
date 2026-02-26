package org.dromara.datacenter.nephrology.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.datacenter.nephrology.domain.HdPatient;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Hemodialysis patient view object.
 */
@Data
@AutoMapper(target = HdPatient.class)
public class HdPatientVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String inpatientNo;
    private String outpatientNo;
    private String name;
    private String gender;
    private Integer age;
    private Date birthDate;
    private String idcardMask;
    private String idcardHash;
    private Date admitDate;
    private Date dischargeDate;
    private String admitDept;
    private String dischargeDept;
    private String diagName;
    private String diagCode;
    private Date minHdTime;
    private Date minHdTimeByInpatient;
    private String sourcePatientId;
    private Date sourceUpdatedAt;
    private Date createdAt;
    private Date updatedAt;
    private String idCard;
}
