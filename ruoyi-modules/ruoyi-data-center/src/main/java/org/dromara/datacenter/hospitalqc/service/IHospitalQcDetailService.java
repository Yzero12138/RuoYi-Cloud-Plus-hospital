package org.dromara.datacenter.hospitalqc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDetailQueryBo;

import java.util.List;
import java.util.Map;

/**
 * Detail data query service for ledger SQL.
 */
public interface IHospitalQcDetailService {

    /**
     * Query detail data with pagination.
     *
     * @param bo query conditions
     * @return page of detail rows
     */
    Page<Map<String, Object>> queryDetailPage(HospitalQcDetailQueryBo bo);

    /**
     * Query detail columns (for header configuration).
     *
     * @param ledgerCode ledger code
     * @return list of column names from detail SQL
     */
    List<String> queryDetailColumns(String ledgerCode);
}
