package org.dromara.datacenter.hospitalqc.constants;

/**
 * Constants for hospital quality control ledger module.
 */
public interface HospitalQcConstants {

    String NODE_TYPE_INDICATOR = "I";
    String NODE_TYPE_NUMERATOR = "N";
    String NODE_TYPE_DENOMINATOR = "D";
    String NODE_TYPE_DEPT = "DEPT";

    String QUERY_CODE_NONE = "NONE";

    String TIME_TYPE_YEAR = "year";
    String TIME_TYPE_QUARTER = "quarter";
    String TIME_TYPE_MONTH = "month";
    String TIME_TYPE_CUSTOM = "custom";

    int STATUS_NORMAL = 0;
    int STATUS_DISABLED = 1;

    int LOGIC_NOT_DELETED = 0;
    int LOGIC_DELETED = 1;

    int DEFAULT_SQL_TIMEOUT_SECONDS = 10;
    int DEFAULT_SQL_TEST_LIMIT = 50;
    int DEFAULT_CACHE_SECONDS = 300;
}
