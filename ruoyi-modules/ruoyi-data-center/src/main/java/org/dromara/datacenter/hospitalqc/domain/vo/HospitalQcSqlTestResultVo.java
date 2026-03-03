package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * SQL test response.
 */
@Data
public class HospitalQcSqlTestResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean success;

    private Long elapsedMs;

    private Long countValue;

    private String message;

    private List<String> columns;

    private List<Map<String, Object>> sampleRows;
}

