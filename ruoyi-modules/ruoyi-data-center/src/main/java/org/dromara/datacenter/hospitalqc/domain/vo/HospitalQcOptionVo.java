package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Simple option item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalQcOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String value;

    private String label;
}

