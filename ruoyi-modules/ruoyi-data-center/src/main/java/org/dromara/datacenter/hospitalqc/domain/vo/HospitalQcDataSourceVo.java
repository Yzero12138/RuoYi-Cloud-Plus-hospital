package org.dromara.datacenter.hospitalqc.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Datasource config response.
 */
@Data
public class HospitalQcDataSourceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String sourceName;
    private String sourceType;
    private String host;
    private Integer port;
    private String databaseName;
    private String instanceName;
    private String username;
    private String passwordMasked;
    private Boolean passwordConfigured;
    private Integer status;
    private String remark;

    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
}

