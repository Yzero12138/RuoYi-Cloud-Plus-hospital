package org.dromara.datacenter.hospitalqc.domain.bo;

import lombok.Data;

/**
 * Datasource connection test request.
 */
@Data
public class HospitalQcDataSourceTestBo {

    private Long id;

    private String sourceType;

    private String host;

    private Integer port;

    private String databaseName;

    private String instanceName;

    private String username;

    private String password;
}

