package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Connection test result.
 */
@Data
public class HospitalQcConnectionTestVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean success;

    private Long elapsedMs;

    private String message;
}

