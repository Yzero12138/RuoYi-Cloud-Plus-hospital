package org.dromara.datacenter.nephrology.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.datacenter.nephrology.domain.HemodialysisComplicationPatient;

/**
 * Hemodialysis complication patient query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HemodialysisComplicationPatient.class, reverseConvertGenerate = false)
public class HemodialysisComplicationPatientBo extends BaseEntity {

    private Long id;
    private String medicalRecordNo;
    private String idCardNo;
    /**
     * year / quarter / month.
     */
    private String dischargeTimeType;
    /**
     * year: 2026, quarter: 2026-Q1, month: 2026-01.
     */
    private String dischargeTimeValue;
}
