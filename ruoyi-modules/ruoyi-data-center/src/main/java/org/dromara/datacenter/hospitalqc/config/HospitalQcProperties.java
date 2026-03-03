package org.dromara.datacenter.hospitalqc.config;

import lombok.Data;
import org.dromara.datacenter.hospitalqc.constants.HospitalQcConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Config properties for hospital quality control module.
 */
@Data
@Component
@ConfigurationProperties(prefix = "hospital-qc")
public class HospitalQcProperties {

    /**
     * AES secret used to encrypt datasource passwords.
     */
    private String aesSecret = "hospital-qc-default-secret";

    /**
     * SQL timeout in seconds for query execution and SQL tests.
     */
    private Integer sqlTimeoutSeconds = HospitalQcConstants.DEFAULT_SQL_TIMEOUT_SECONDS;

    /**
     * SQL test max rows.
     */
    private Integer sqlTestLimit = HospitalQcConstants.DEFAULT_SQL_TEST_LIMIT;

    /**
     * Dashboard count cache ttl in seconds.
     */
    private Integer dashboardCacheSeconds = HospitalQcConstants.DEFAULT_CACHE_SECONDS;

    /**
     * SQLServer TLS mode.
     * TLS12: enforce TLSv1.2 (recommended, default)
     * LEGACY_NO_ENCRYPT: disable encryption for legacy servers (temporary workaround)
     * LEGACY_TLS10: try TLSv1.0 (requires JVM security policy compatibility)
     */
    private String sqlserverTlsMode = "TLS12";
}

