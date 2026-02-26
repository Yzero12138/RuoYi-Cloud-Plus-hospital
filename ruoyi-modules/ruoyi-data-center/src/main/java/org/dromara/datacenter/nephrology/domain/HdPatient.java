package org.dromara.datacenter.nephrology.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Hemodialysis patient table hd_patient.
 */
@Data
@TableName("hd_patient")
public class HdPatient {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String inpatientNo;
    private String outpatientNo;
    private String name;
    /**
     * 1 male, 2 female.
     */
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
    /**
     * Keep for legacy compatibility.
     */
    private String idCard;
}
