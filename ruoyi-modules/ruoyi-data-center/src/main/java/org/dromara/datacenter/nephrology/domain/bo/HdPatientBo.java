package org.dromara.datacenter.nephrology.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.datacenter.nephrology.domain.HdPatient;

/**
 * Hemodialysis patient query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HdPatient.class, reverseConvertGenerate = false)
public class HdPatientBo extends BaseEntity {

    private Long id;
    private String inpatientNo;
    private String outpatientNo;
    private String name;
    private String gender;
    private String idcardHash;
    private String sourcePatientId;
}
